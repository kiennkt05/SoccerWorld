package com.example.soccerworld.data

import android.util.Log
import com.example.soccerworld.data.cache.CacheTtl
import com.example.soccerworld.data.local.FootballDao
import com.example.soccerworld.data.local.entity.FavoriteMatchEntity
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.data.model.ErrorType
import com.example.soccerworld.data.remote.ApiService
import com.example.soccerworld.data.remote.flashlive.EventStatsResponse
import com.example.soccerworld.data.remote.flashlive.EventSummaryResponse
import com.example.soccerworld.data.remote.flashlive.FlashLiveEvent
import com.example.soccerworld.data.remote.flashlive.LineupsResponse
import com.example.soccerworld.model.fixture.AwayTeam
import com.example.soccerworld.model.fixture.Competition
import com.example.soccerworld.model.fixture.FixtureResponse
import com.example.soccerworld.model.fixture.FullTime
import com.example.soccerworld.model.fixture.HomeTeam
import com.example.soccerworld.model.fixture.Matche
import com.example.soccerworld.model.fixture.ResultSet
import com.example.soccerworld.model.fixture.Score
import com.example.soccerworld.model.h2h.AwayTeamX
import com.example.soccerworld.model.h2h.FullTime as H2HFullTime
import com.example.soccerworld.model.h2h.H2HResponse
import com.example.soccerworld.model.h2h.HomeTeamX
import com.example.soccerworld.model.h2h.Matche as H2HMatch
import com.example.soccerworld.model.h2h.Score as H2HScore
import com.example.soccerworld.model.leaguetable.LeagueTableResponse
import com.example.soccerworld.model.leaguetable.Standing
import com.example.soccerworld.model.leaguetable.Table
import com.example.soccerworld.model.leaguetable.Team
import com.example.soccerworld.model.matchdetail.MatchLineupPlayer
import com.example.soccerworld.model.matchdetail.MatchLineupTeam
import com.example.soccerworld.model.matchdetail.MatchEnrichmentDetail
import com.example.soccerworld.model.matchdetail.MatchEvent
import com.example.soccerworld.model.matchdetail.MatchStatItem
import com.example.soccerworld.model.matchdetail.MatchDetailAggregate
import com.example.soccerworld.model.player.PlayerResponse
import com.example.soccerworld.model.player.Squad
import com.example.soccerworld.model.statistic.AwayTeam as StatsAwayTeam
import com.example.soccerworld.model.statistic.FullTime as StatsFullTime
import com.example.soccerworld.model.statistic.HomeTeam as StatsHomeTeam
import com.example.soccerworld.model.statistic.Score as StatsScore
import com.example.soccerworld.model.statistic.StatisticsResponse
import com.example.soccerworld.model.team.Team as TeamModel
import com.example.soccerworld.model.team.TeamResponse
import com.example.soccerworld.model.topscorer.TopScorerEntity
import com.example.soccerworld.util.Constant
import com.example.soccerworld.util.CustomSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FootballRepository(
    private val apiService: ApiService,
    private val dao: FootballDao,
    private val customPreferences: CustomSharedPreferences
) {
    private val leagueTableCache = mutableMapOf<String, LeagueTableResponse>()
    private val fixtureCache = mutableMapOf<String, FixtureResponse>()
    private val teamsCache = mutableMapOf<String, TeamResponse>()

    fun getSelectedLeagueId(): String = customPreferences.getLeagueId() ?: "PL"

    suspend fun getLeagueTable(leagueId: String): DataResult<LeagueTableResponse> {
        val league = Constant.league(leagueId)
            ?: return DataResult.Error(ErrorType.NOT_FOUND, "Unsupported league code: $leagueId")
        val updateTime = customPreferences.getStandingsTime() ?: 0L
        val now = System.currentTimeMillis()
        val isCacheValid = (now - updateTime) < CacheTtl.STANDINGS_MS
        leagueTableCache[leagueId]?.takeIf { isCacheValid }?.let {
            return DataResult.Success(it, fromCache = true)
        }
        return safeApiCall {
            val rows = mutableListOf<Table>()
            for (page in 1..2) {
                val response = apiService.getStandings(Constant.LOCALE, "overall", league.stageId, league.seasonId, page)
                val pageRows = response.data?.firstOrNull()?.rows.orEmpty().map { row ->
                    val goals = parseGoals(row.goals)
                    Table(
                        position = row.ranking,
                        team = Team(id = row.teamId, name = row.teamName, shortName = row.teamName, crest = row.teamImagePath),
                        playedGames = row.matchesPlayed,
                        won = row.wins,
                        draw = row.draws,
                        lost = row.losses,
                        points = row.points,
                        goalsFor = goals.first,
                        goalsAgainst = goals.second,
                        goalDifference = (goals.first ?: 0) - (goals.second ?: 0)
                    )
                }
                rows.addAll(pageRows)
                if (pageRows.size < 10) break
            }
            LeagueTableResponse(standings = listOf(Standing(type = "TOTAL", table = rows))).also {
                leagueTableCache[leagueId] = it
                customPreferences.saveStandingsTime(now)
            }
        }
    }

    suspend fun getTopScorers(leagueId: String): DataResult<List<TopScorerEntity>> {
        val league = Constant.league(leagueId)
            ?: return DataResult.Error(ErrorType.NOT_FOUND, "Unsupported league code: $leagueId")
        val updateTime = customPreferences.getTopScorersTime() ?: 0L
        val now = System.currentTimeMillis()
        val isCacheValid = (now - updateTime) < CacheTtl.TOP_SCORERS_MS
        if (isCacheValid) {
            val localData = dao.getTopScorers()
            if (localData.isNotEmpty()) return DataResult.Success(localData, fromCache = true)
        }
        return safeApiCall {
            val entities = apiService.getTopScorers(Constant.LOCALE, "top_scores", league.stageId, league.seasonId)
                .data?.firstOrNull()?.rows.orEmpty()
                .mapNotNull {
                    val playerId = it.playerId ?: return@mapNotNull null
                    TopScorerEntity(
                        playerId = playerId,
                        playerName = it.playerName ?: "Unknown Player",
                        teamName = it.teamName ?: "Unknown Team",
                        goals = it.goals ?: 0,
                        imageUrl = it.imagePath
                    )
                }
                .sortedByDescending { it.goals }
                .take(10)
            dao.clearTopScorers()
            dao.insertTopScorers(entities)
            customPreferences.saveTopScorersTime(now)
            customPreferences.saveTime(now)
            entities
        }
    }

    suspend fun getAllTeamsOfLeague(leagueId: String): DataResult<TeamResponse> {
        val league = Constant.league(leagueId)
            ?: return DataResult.Error(ErrorType.NOT_FOUND, "Unsupported league code: $leagueId")
        val updateTime = customPreferences.getTeamsTime() ?: 0L
        val now = System.currentTimeMillis()
        val isCacheValid = (now - updateTime) < CacheTtl.TEAM_INFO_MS
        teamsCache[leagueId]?.takeIf { isCacheValid }?.let { return DataResult.Success(it, fromCache = true) }
        return safeApiCall {
            val rows = mutableListOf<TeamModel>()
            for (page in 1..2) {
                val response = apiService.getStandings(Constant.LOCALE, "overall", league.stageId, league.seasonId, page)
                val pageRows = response.data?.firstOrNull()?.rows.orEmpty().map {
                    TeamModel(id = it.teamId, name = it.teamName, shortName = it.teamName, crest = it.teamImagePath)
                }
                rows.addAll(pageRows)
                if (pageRows.size < 10) break
            }
            TeamResponse(count = rows.size, teams = rows).also {
                teamsCache[leagueId] = it
                customPreferences.saveTeamsTime(now)
            }
        }
    }

    suspend fun getAllPlayersOfTeam(teamId: String): DataResult<PlayerResponse> {
        return safeApiCall {
            val teamData = apiService.getTeamData(Constant.LOCALE, Constant.SPORT_ID, teamId).data
            val players = apiService.getTeamSquad(Constant.LOCALE, Constant.SPORT_ID, teamId)
                .data.orEmpty()
                .flatMap { it.items.orEmpty() }
                .map {
                    Squad(
                        id = it.playerId,
                        name = it.playerName,
                        position = mapPlayerType(it.playerTypeId),
                        imageUrl = it.playerImagePath
                    )
                }
            PlayerResponse(id = teamData?.id, name = teamData?.name, crest = teamData?.imagePath, squad = players)
        }
    }

    suspend fun preloadPlayerMediaInParallel(players: List<TopScorerEntity>): Map<String, String?> {
        return players.associate { it.playerId to it.imageUrl }
    }

    suspend fun getAllFixtureOfLeague(
        leagueId: String,
        dateFrom: String? = null,
        dateTo: String? = null,
        stage: String? = null,
        status: String? = null,
        matchday: Int? = null,
        forceRefresh: Boolean = false
    ): DataResult<FixtureResponse> {
        val league = Constant.league(leagueId)
            ?: return DataResult.Error(ErrorType.NOT_FOUND, "Unsupported league code: $leagueId")
        val updateTime = customPreferences.getFixturesTime() ?: 0L
        val now = System.currentTimeMillis()
        val isCacheValid = (now - updateTime) < CacheTtl.FIXTURES_MS
        val cacheKey = listOf(leagueId, dateFrom, dateTo, stage, status, matchday).joinToString("|")
        fixtureCache[cacheKey]?.takeIf { isCacheValid && !forceRefresh }?.let { return DataResult.Success(it, fromCache = true) }

        return safeApiCall {
            val events = fetchTournamentEvents(league.stageId, status)
            val mappedMatches = events
                .map { it.toFixtureMatch(league.name, leagueId) }
                .filter { match ->
                    val dateFilter = if (!dateFrom.isNullOrBlank() && !dateTo.isNullOrBlank()) {
                        val matchDate = match.utcDate?.take(10)
                        matchDate != null && matchDate >= dateFrom && matchDate <= dateTo
                    } else true
                    val statusFilter = status?.let { it == match.status } ?: true
                    val matchdayFilter = matchday?.let { it == match.matchday } ?: true
                    val stageFilter = stage?.let { it.equals(match.stage, ignoreCase = true) } ?: true
                    dateFilter && statusFilter && matchdayFilter && stageFilter
                }
            FixtureResponse(
                competition = Competition(code = leagueId, name = league.name),
                resultSet = ResultSet(count = mappedMatches.size),
                matches = mappedMatches
            ).also {
                fixtureCache[cacheKey] = it
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
    fun observeIsFavorite(matchId: String): Flow<Boolean> = dao.observeIsFavorite(matchId)

    suspend fun getAllH2hItems(fixtureId: String): DataResult<H2HResponse> {
        return safeApiCall {
            val items = apiService.getHeadToHead(Constant.LOCALE, fixtureId)
                .data?.firstOrNull()
                ?.groups?.firstOrNull()
                ?.items.orEmpty()
            H2HResponse(
                matches = items.map {
                    val scores = parseScorePair(it.currentResult)
                    H2HMatch(
                        id = it.eventId,
                        utcDate = toIsoDateTime(it.startTime),
                        homeTeam = HomeTeamX(name = it.homeParticipant),
                        awayTeam = AwayTeamX(name = it.awayParticipant),
                        score = H2HScore(fullTime = H2HFullTime(home = scores.first, away = scores.second))
                    )
                }
            )
        }
    }

    suspend fun getFixtureStatistics(fixtureId: String): DataResult<StatisticsResponse> {
        return safeApiCall {
            val event = apiService.getEventData(Constant.LOCALE, fixtureId).data
            StatisticsResponse(
                id = event?.eventId,
                utcDate = toIsoDateTime(event?.startTime),
                status = mapStatus(event?.stageType),
                homeTeam = StatsHomeTeam(id = event?.homeId, name = event?.homeName, crest = event?.homeImagePath),
                awayTeam = StatsAwayTeam(id = event?.awayId, name = event?.awayName, crest = event?.awayImagePath),
                score = StatsScore(
                    fullTime = StatsFullTime(
                        home = event?.homeScore?.toIntOrNull(),
                        away = event?.awayScore?.toIntOrNull()
                    )
                )
            )
        }
    }

    suspend fun getMatchDetailAggregate(fixtureId: String): DataResult<MatchDetailAggregate> {
        val coreResult = getFixtureStatistics(fixtureId)
        if (coreResult !is DataResult.Success) {
            return when (coreResult) {
                is DataResult.Error -> DataResult.Error(coreResult.type, coreResult.message)
                else -> DataResult.Error(ErrorType.UNKNOWN, "Failed to load core match detail")
            }
        }
        val h2hResult = getAllH2hItems(fixtureId)
        val h2hList = (h2hResult as? DataResult.Success)?.data?.matches ?: emptyList()
        val enrichmentResult = safeApiCall {
            val summary = apiService.getEventSummary(Constant.LOCALE, fixtureId)
            val stats = apiService.getEventStats(Constant.LOCALE, fixtureId)
            val lineups = apiService.getEventLineups(Constant.LOCALE, fixtureId)
            mapFlashLiveDetail(fixtureId, summary, stats, lineups)
        }
        val enrichment = (enrichmentResult as? DataResult.Success)?.data
        return DataResult.Success(
            MatchDetailAggregate(core = coreResult.data, h2h = h2hList, enrichment = enrichment),
            fromCache = false
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

    private fun mapFlashLiveDetail(
        eventId: String,
        summary: EventSummaryResponse,
        stats: EventStatsResponse,
        lineups: LineupsResponse
    ): MatchEnrichmentDetail {
        val events = summary.data.orEmpty().flatMap { stage ->
            stage.items.orEmpty().flatMap { item ->
                item.participants.orEmpty().map { participant ->
                    MatchEvent(
                        minute = item.time ?: "--",
                        type = participant.type ?: participant.incidentName ?: "Event",
                        description = participant.participantName ?: participant.incidentName ?: "Event",
                        team = null
                    )
                }
            }
        }
        val statItems = stats.data.orEmpty().flatMap { stage ->
            stage.groups.orEmpty().flatMap { group ->
                group.items.orEmpty().map { item ->
                    MatchStatItem(
                        name = item.incidentName ?: "Stat",
                        homeValue = item.valueHome ?: "-",
                        awayValue = item.valueAway ?: "-"
                    )
                }
            }
        }
        val lineupTeams = lineups.data.orEmpty().flatMap { group ->
            group.formations.orEmpty().map { formation ->
                val players = formation.members.orEmpty().map { member ->
                    MatchLineupPlayer(
                        name = member.fullName ?: "Player",
                        position = member.positionId?.toString(),
                        imageUrl = null
                    )
                }
                MatchLineupTeam(
                    teamName = if (formation.playerGroupType == 1) "Home" else "Away",
                    formation = formation.formation,
                    starters = players,
                    substitutes = emptyList()
                )
            }
        }
        return MatchEnrichmentDetail(
            eventId = eventId,
            venue = summary.info?.venue,
            events = events,
            stats = statItems,
            lineups = lineupTeams,
            status = null,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun FlashLiveEvent.toFixtureMatch(leagueName: String, leagueCode: String): Matche {
        val homeCrest = homeImagePath ?: homeImages?.firstOrNull()
        val awayCrest = awayImagePath ?: awayImages?.firstOrNull()
        return Matche(
            id = eventId,
            utcDate = toIsoDateTime(startTime),
            status = mapStatus(stageType),
            matchday = round?.toIntOrNull(),
            stage = stageType,
            group = round ?: "Unknown Round",
            competition = Competition(code = leagueCode, name = leagueName),
            homeTeam = HomeTeam(id = homeId, name = homeName, shortName = homeName, crest = homeCrest),
            awayTeam = AwayTeam(id = awayId, name = awayName, shortName = awayName, crest = awayCrest),
            score = Score(fullTime = FullTime(home = homeScore?.toIntOrNull(), away = awayScore?.toIntOrNull()))
        )
    }

    private suspend fun fetchTournamentEvents(stageId: String, status: String?): List<FlashLiveEvent> {
        val useResultsOnly = status == "FINISHED"
        val useFixturesOnly = status == "SCHEDULED" || status == "IN_PLAY" || status == "LIVE" || status == "PAUSED"
        val events = mutableListOf<FlashLiveEvent>()

        if (!useResultsOnly) {
            events += fetchPagedEvents { page ->
                apiService.getTournamentFixtures(Constant.LOCALE, stageId, page)
                    .data.orEmpty()
                    .flatMap { it.events.orEmpty() }
            }
        }
        if (!useFixturesOnly) {
            events += fetchPagedEvents { page ->
                apiService.getTournamentResults(Constant.LOCALE, stageId, page)
                    .data.orEmpty()
                    .flatMap { it.events.orEmpty() }
            }
        }
        return events.distinctBy { it.eventId }
    }

    private suspend fun fetchPagedEvents(fetchPage: suspend (Int) -> List<FlashLiveEvent>): List<FlashLiveEvent> {
        val merged = mutableListOf<FlashLiveEvent>()
        for (page in 1..10) {
            val pageItems = fetchPage(page)
            if (pageItems.isEmpty()) break
            merged += pageItems
            if (pageItems.size < 20) break
        }
        return merged
    }

    private fun parseGoals(goals: String?): Pair<Int?, Int?> {
        val parts = goals?.split(":").orEmpty()
        if (parts.size != 2) return null to null
        return parts[0].trim().toIntOrNull() to parts[1].trim().toIntOrNull()
    }

    private fun parseScorePair(score: String?): Pair<Int?, Int?> {
        val parts = score?.split(":").orEmpty()
        if (parts.size != 2) return null to null
        return parts[0].trim().toIntOrNull() to parts[1].trim().toIntOrNull()
    }

    private fun mapStatus(stageType: String?): String {
        return when (stageType?.uppercase(Locale.US)) {
            "LIVE", "IN_PLAY" -> "IN_PLAY"
            "PAUSED", "HALFTIME" -> "PAUSED"
            "FINISHED", "FT" -> "FINISHED"
            else -> "SCHEDULED"
        }
    }

    private fun mapPlayerType(typeId: String?): String {
        val normalized = typeId?.trim()?.uppercase(Locale.US).orEmpty()
        return when (normalized) {
            "1", "GOALKEEPER", "GK" -> "Goalkeeper"
            "2", "DEFENDER", "DF" -> "Defender"
            "3", "MIDFIELDER", "MF" -> "Midfielder"
            "4", "FORWARD", "ATTACKER", "FW", "ST" -> "Forward"
            else -> "Player"
        }
    }

    private fun toIsoDateTime(unixSeconds: Long?): String? {
        unixSeconds ?: return null
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(unixSeconds * 1000L))
    }
}
