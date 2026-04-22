package com.example.soccerworld.data

import android.util.Log
import com.example.soccerworld.BuildConfig
import com.example.soccerworld.data.cache.CacheTtl
import com.example.soccerworld.data.local.FootballDao
import com.example.soccerworld.data.local.entity.FavoriteMatchEntity
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.data.model.ErrorType
import com.example.soccerworld.data.remote.ApiService
import com.example.soccerworld.data.repository.MediaRepository
import com.example.soccerworld.data.repository.EspnEnrichmentRepository
import com.example.soccerworld.model.fixture.AwayTeam
import com.example.soccerworld.model.fixture.FixtureResponse
import com.example.soccerworld.model.fixture.HomeTeam
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.model.h2h.H2HResponse
import com.example.soccerworld.model.matchdetail.MatchDetailAggregate
import com.example.soccerworld.model.leaguetable.LeagueTableResponse
import com.example.soccerworld.model.player.PlayerResponse
import com.example.soccerworld.model.statistic.StatisticsResponse
import com.example.soccerworld.model.team.TeamResponse
import com.example.soccerworld.model.topscorer.TopScorerEntity
import com.example.soccerworld.util.CustomSharedPreferences
import com.example.soccerworld.util.toEntityList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class FootballRepository(
    private val apiService: ApiService,
    private val dao: FootballDao,
    private val customPreferences: CustomSharedPreferences,
    private val mediaRepository: MediaRepository,
    private val espnEnrichmentRepository: EspnEnrichmentRepository
) {
    private val apiKey: String by lazy { BuildConfig.FOOTBALL_DATA_API_KEY.trim() }
    private val leagueTableCache = mutableMapOf<String, LeagueTableResponse>()
    private val fixtureCache = mutableMapOf<String, FixtureResponse>()
    private val teamsCache = mutableMapOf<String, TeamResponse>()

    // Lấy LeagueId đang chọn
    fun getSelectedLeagueId(): String = customPreferences.getLeagueId() ?: "2021"

    // 1. BẢNG XẾP HẠNG
    suspend fun getLeagueTable(
        leagueId: String,
        season: String? = null,
        matchday: Int? = null,
        date: String? = null
    ): DataResult<LeagueTableResponse> {
        Log.d("FootballRepository", "getLeagueTable: leagueId=$leagueId")
        if (!hasApiKey()) {
            Log.e("FootballRepository", "Missing API Key")
            return missingApiKeyError()
        }

        val updateTime = customPreferences.getStandingsTime() ?: 0L
        val now = System.currentTimeMillis()
        val isCacheValid = (now - updateTime) < CacheTtl.STANDINGS_MS
        leagueTableCache[leagueId]?.takeIf { isCacheValid }?.let {
            Log.d("FootballRepository", "Returning from cache")
            return DataResult.Success(it, fromCache = true)
        }

        return safeApiCall {
            Log.d("FootballRepository", "Calling API for League Table")
            val response = apiService.getLeagueTable(apiKey, leagueId, season, matchday, date)
            val enriched = enrichLeagueTableMedia(response)
            enriched.also {
                leagueTableCache[leagueId] = enriched
                customPreferences.saveStandingsTime(now)
            }
        }
    }

    // 2. VUA PHÁ LƯỚI
    suspend fun getTopScorers(leagueId: String): DataResult<List<TopScorerEntity>> {
        if (!hasApiKey()) return missingApiKeyError()

        val updateTime = customPreferences.getTopScorersTime() ?: 0L
        val now = System.currentTimeMillis()
        val isCacheValid = (now - updateTime) < CacheTtl.TOP_SCORERS_MS

        if (isCacheValid) {
            val localData = dao.getTopScorers()
            if (localData.isNotEmpty()) {
                return DataResult.Success(localData, fromCache = true)
            }
        }

        return safeApiCall {
            val response = apiService.getTopScorers(apiKey, leagueId)
            val entities = response.toEntityList()
            dao.clearTopScorers()
            dao.insertTopScorers(entities)
            customPreferences.saveTopScorersTime(now)
            // Keep legacy key in sync for backward compatibility.
            customPreferences.saveTime(now)
            entities
        }
    }

    // 3. DANH SÁCH ĐỘI BÓNG
    suspend fun getAllTeamsOfLeague(
        leagueId: String,
        season: String? = null
    ): DataResult<TeamResponse> {
        if (!hasApiKey()) return missingApiKeyError()

        val updateTime = customPreferences.getTeamsTime() ?: 0L
        val now = System.currentTimeMillis()
        val isCacheValid = (now - updateTime) < CacheTtl.TEAM_INFO_MS
        teamsCache[leagueId]?.takeIf { isCacheValid }?.let {
            return DataResult.Success(it, fromCache = true)
        }

        return safeApiCall {
            val response = apiService.getAllTeamsOfLeague(apiKey, leagueId, season)
            val enriched = enrichTeamsMedia(response)
            enriched.also {
                teamsCache[leagueId] = enriched
                customPreferences.saveTeamsTime(now)
            }
        }
    }

    // 4. CHI TIẾT ĐỘI BÓNG (Không có filter)
    suspend fun getAllPlayersOfTeam(
        teamId: Int
    ): DataResult<PlayerResponse> {
        if (!hasApiKey()) return missingApiKeyError()
        return safeApiCall {
            val response = apiService.getAllPlayersOfTeam(apiKey, teamId)
            val mappedSquad = coroutineScope {
                response.squad?.map { player ->
                    async {
                        player?.let { squad ->
                            val media = mediaRepository.resolvePlayerMedia(squad.id, squad.name)
                            val mediaEntity = (media as? DataResult.Success)?.data
                            val imageUrl = mediaEntity?.thumbUrl ?: mediaEntity?.cutoutUrl
                            squad.copy(imageUrl = imageUrl)
                        }
                    }
                }?.awaitAll()
            }
            response.copy(squad = mappedSquad)
        }
    }

    suspend fun preloadPlayerMediaInParallel(players: List<TopScorerEntity>): Map<Int, String?> = coroutineScope {
        players.map { scorer ->
            async {
                val media = mediaRepository.resolvePlayerMedia(scorer.playerId, scorer.playerName)
                val mediaEntity = (media as? DataResult.Success)?.data
                scorer.playerId to (mediaEntity?.thumbUrl ?: mediaEntity?.cutoutUrl)
            }
        }.awaitAll().toMap()
    }

    // 5. LỊCH THI ĐẤU
    suspend fun getAllFixtureOfLeague(
        leagueId: String,
        dateFrom: String? = null,
        dateTo: String? = null,
        stage: String? = null,
        status: String? = null,
        matchday: Int? = null,
        group: String? = null,
        season: String? = null,
        forceRefresh: Boolean = false
    ): DataResult<FixtureResponse> {
        if (!hasApiKey()) return missingApiKeyError()

        val updateTime = customPreferences.getFixturesTime() ?: 0L
        val now = System.currentTimeMillis()
        val isCacheValid = (now - updateTime) < CacheTtl.FIXTURES_MS
        val cacheKey = listOf(leagueId, dateFrom, dateTo, stage, status, matchday, group, season).joinToString("|")
        fixtureCache[cacheKey]?.takeIf { isCacheValid && !forceRefresh }?.let {
            return DataResult.Success(it, fromCache = true)
        }

        return safeApiCall {
            val response = apiService.getAllFixtureOfLeague(apiKey, leagueId, dateFrom, dateTo, stage, status, matchday, group, season)
            val enriched = enrichFixtureMedia(response)
            enriched.also {
                fixtureCache[cacheKey] = enriched
                customPreferences.saveFixturesTime(now)
            }
        }
    }

    suspend fun toggleFavorite(match: Matche) {
        val id = match.id ?: return
        if (dao.isFavorite(id)) {
            dao.deleteFavorite(id)
            return
        }
        dao.insertFavorite(
            FavoriteMatchEntity(
                matchId = id,
                leagueCode = match.competition?.code,
                utcDate = match.utcDate,
                homeTeamId = match.homeTeam?.id,
                homeTeamName = match.homeTeam?.name,
                homeTeamCrest = match.homeTeam?.crest,
                awayTeamId = match.awayTeam?.id,
                awayTeamName = match.awayTeam?.name,
                awayTeamCrest = match.awayTeam?.crest,
                status = match.status,
                savedAt = System.currentTimeMillis()
            )
        )
    }

    fun observeFavorites(): Flow<List<FavoriteMatchEntity>> = dao.getAllFavorites()

    fun observeIsFavorite(matchId: Int): Flow<Boolean> = dao.observeIsFavorite(matchId)

    // 6. LỊCH SỬ ĐỐI ĐẦU
    suspend fun getAllH2hItems(
        fixtureId: Int,
        limit: Int? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        competitions: String? = null
    ): DataResult<H2HResponse> {
        if (!hasApiKey()) return missingApiKeyError()
        return safeApiCall {
            apiService.getAllH2hItems(apiKey, fixtureId, limit, dateFrom, dateTo, competitions)
        }
    }

    // 7. CHI TIẾT TRẬN ĐẤU (Không có filter)
    suspend fun getFixtureStatistics(
        fixtureId: Int
    ): DataResult<StatisticsResponse> {
        if (!hasApiKey()) return missingApiKeyError()
        return safeApiCall {
            apiService.getFixtureStatistics(apiKey, fixtureId)
        }
    }

    suspend fun getMatchDetailAggregate(fixtureId: Int): DataResult<MatchDetailAggregate> {
        if (!hasApiKey()) return missingApiKeyError()
        Log.d("FootballRepository", "getMatchDetailAggregate fixtureId=$fixtureId")

        val coreResult = getFixtureStatistics(fixtureId)
        if (coreResult !is DataResult.Success) {
            return when (coreResult) {
                is DataResult.Error -> DataResult.Error(coreResult.type, coreResult.message)
                else -> DataResult.Error(ErrorType.UNKNOWN, "Failed to load core match detail")
            }
        }

        val h2hResult = getAllH2hItems(fixtureId)
        val h2hList = (h2hResult as? DataResult.Success)?.data?.matches ?: emptyList()
        Log.d("FootballRepository", "Aggregate coreLoaded=true h2hCount=${h2hList.size}")

        val enrichmentResult = espnEnrichmentRepository.getMatchEnrichment(coreResult.data, fixtureId)
        val enrichment = (enrichmentResult as? DataResult.Success)?.data
        Log.d(
            "FootballRepository",
            "Aggregate enrichment events=${enrichment?.events?.size ?: 0} stats=${enrichment?.stats?.size ?: 0} lineups=${enrichment?.lineups?.size ?: 0}"
        )

        return DataResult.Success(
            MatchDetailAggregate(
                core = coreResult.data,
                h2h = h2hList,
                enrichment = enrichment
            ),
            fromCache = (enrichmentResult as? DataResult.Success)?.fromCache == true
        )
    }

    private suspend fun <T> safeApiCall(block: suspend () -> T): DataResult<T> = withContext(Dispatchers.IO) {
        try {
            DataResult.Success(block())
        } catch (io: IOException) {
            Log.e("FootballRepository", "Network Error: ${io.message}")
            DataResult.Error(ErrorType.NETWORK, io.message)
        } catch (http: HttpException) {
            Log.e("FootballRepository", "HTTP Error: ${http.code()} ${http.message()}")
            DataResult.Error(mapHttpError(http.code()), http.message())
        } catch (e: Exception) {
            Log.e("FootballRepository", "Unknown Error: ${e.message}")
            DataResult.Error(ErrorType.UNKNOWN, e.message)
        }
    }

    private fun mapHttpError(code: Int): ErrorType {
        return when (code) {
            403, 429 -> ErrorType.RATE_LIMITED
            404 -> ErrorType.NOT_FOUND
            else -> ErrorType.UNKNOWN
        }
    }

    private fun hasApiKey(): Boolean = apiKey.isNotBlank()

    private fun <T> missingApiKeyError(): DataResult<T> {
        return DataResult.Error(
            type = ErrorType.UNKNOWN,
            message = "Missing FOOTBALL_DATA_API_KEY in local.properties."
        )
    }

    private suspend fun enrichTeamsMedia(response: TeamResponse): TeamResponse {
        val mappedTeams = response.teams?.map { team ->
            team?.let { t ->
                val media = mediaRepository.resolveTeamMedia(t.id, t.name, t.crest)
                val resolvedCrest = (media as? DataResult.Success)?.data?.badgeUrl ?: t.crest
                t.copy(crest = resolvedCrest)
            }
        }
        return response.copy(teams = mappedTeams)
    }

    private suspend fun enrichLeagueTableMedia(response: LeagueTableResponse): LeagueTableResponse {
        val mappedStandings = response.standings?.map { standing ->
            standing?.copy(
                table = standing.table?.map { row ->
                    row?.let { item ->
                        val team = item.team
                        val media = mediaRepository.resolveTeamMedia(team?.id, team?.name, team?.crest)
                        val resolvedCrest = (media as? DataResult.Success)?.data?.badgeUrl ?: team?.crest
                        val mappedTeam = team?.copy(crest = resolvedCrest)
                        item.copy(team = mappedTeam)
                    }
                }
            )
        }
        return response.copy(standings = mappedStandings)
    }

    private suspend fun enrichFixtureMedia(response: FixtureResponse): FixtureResponse {
        val mappedMatches = response.matches?.map { match -> mapMatchMedia(match) }
        return response.copy(matches = mappedMatches)
    }

    private suspend fun mapMatchMedia(match: Matche): Matche {
        val home = mapHomeTeamMedia(match.homeTeam)
        val away = mapAwayTeamMedia(match.awayTeam)
        return match.copy(homeTeam = home, awayTeam = away)
    }

    private suspend fun mapHomeTeamMedia(homeTeam: HomeTeam?): HomeTeam? {
        homeTeam ?: return null
        val media = mediaRepository.resolveTeamMedia(homeTeam.id, homeTeam.name, homeTeam.crest)
        val resolvedCrest = (media as? DataResult.Success)?.data?.badgeUrl ?: homeTeam.crest
        return homeTeam.copy(crest = resolvedCrest)
    }

    private suspend fun mapAwayTeamMedia(awayTeam: AwayTeam?): AwayTeam? {
        awayTeam ?: return null
        val media = mediaRepository.resolveTeamMedia(awayTeam.id, awayTeam.name, awayTeam.crest)
        val resolvedCrest = (media as? DataResult.Success)?.data?.badgeUrl ?: awayTeam.crest
        return awayTeam.copy(crest = resolvedCrest)
    }
}