package com.example.soccerworld.data.repository

import android.util.Log
import com.example.soccerworld.data.cache.CacheTtl
import com.example.soccerworld.data.local.FootballDao
import com.example.soccerworld.data.local.entity.EspnMatchEnrichmentEntity
import com.example.soccerworld.data.local.entity.IdBridgeEntity
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.data.model.ErrorType
import com.example.soccerworld.data.remote.RetryPolicy
import com.example.soccerworld.data.remote.espn.EspnApi
import com.example.soccerworld.data.remote.espn.EspnLeague
import com.example.soccerworld.model.matchdetail.EspnLineupPlayer
import com.example.soccerworld.model.matchdetail.EspnLineupTeam
import com.example.soccerworld.model.matchdetail.EspnMatchDetail
import com.example.soccerworld.model.matchdetail.EspnMatchEvent
import com.example.soccerworld.model.matchdetail.EspnStatItem
import com.example.soccerworld.model.statistic.StatisticsResponse
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.HttpException
import java.io.IOException
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class EspnEnrichmentRepository(
    private val espnApi: EspnApi,
    private val dao: FootballDao
) {
    private val tag = "EspnEnrichmentRepo"
    private val gson = Gson()
    private val teamAliases = mapOf(
        "psg" to "paris saint germain",
        "man utd" to "manchester united",
        "club atletico madrid" to "atletico madrid",
        "atletico madrid" to "atletico madrid",
        "sporting e portugal" to "sporting cp",
        "sporting portugal" to "sporting cp",
        "bayern munchen" to "bayern munich",
        "inter milano" to "internazionale",
        "sunrland a" to "sunderland",
        "spurs" to "tottenham hotspur"
    )

    suspend fun getMatchEnrichment(core: StatisticsResponse?, fixtureId: Int): DataResult<EspnMatchDetail?> {
        if (core == null) return DataResult.Success(null)
        Log.d(tag, "getMatchEnrichment fixtureId=$fixtureId status=${core.status} competition=${core.competition?.code}")

        val cached = dao.getEspnMatchEnrichment(fixtureId)
        if (cached != null) {
            val ttl = if (core.status == "FINISHED") CacheTtl.ESPN_ENRICHMENT_FINISHED_MS else CacheTtl.ESPN_ENRICHMENT_LIVE_MS
            if (System.currentTimeMillis() - cached.lastUpdated <= ttl) {
                Log.d(tag, "Using cached enrichment fixtureId=$fixtureId ageMs=${System.currentTimeMillis() - cached.lastUpdated}")
                return DataResult.Success(parseDetailFromEntity(cached), fromCache = true)
            }
        }

        return try {
            val leagueSlug = EspnLeague.fromFdCode(core.competition?.code)?.slug
            if (leagueSlug == null) {
                Log.w(tag, "Missing ESPN league mapping for fdCode=${core.competition?.code}")
                return DataResult.Error(ErrorType.ENRICHMENT_FAIL, "Unsupported competition for ESPN mapping")
            }

            val eventId = resolveEspnEventId(core, fixtureId, leagueSlug)
            if (eventId == null) return DataResult.Error(ErrorType.ENRICHMENT_FAIL, "Unable to resolve ESPN event id")

            Log.d(tag, "Calling ESPN summary league=$leagueSlug eventId=$eventId")
            val summary = RetryPolicy.executeWithBackoff { espnApi.getSummary(leagueSlug, eventId) }
            val payload = gson.toJson(summary)
            val detail = mapSummaryToDetail(summary, eventId)
            Log.d(
                tag,
                "Mapped enrichment eventId=$eventId events=${detail.events.size} stats=${detail.stats.size} lineups=${detail.lineups.size}"
            )
            dao.insertEspnMatchEnrichment(
                EspnMatchEnrichmentEntity(
                    footballDataMatchId = fixtureId,
                    espnEventId = eventId,
                    payloadJson = payload,
                    status = detail.status,
                    lastUpdated = System.currentTimeMillis()
                )
            )
            DataResult.Success(detail)
        } catch (http: HttpException) {
            val errType = if (http.code() in setOf(403, 429)) ErrorType.RATE_LIMITED else ErrorType.ENRICHMENT_FAIL
            Log.e(tag, "HTTP error enrichment fixtureId=$fixtureId code=${http.code()} message=${http.message()}")
            DataResult.Error(errType, http.message())
        } catch (io: IOException) {
            Log.e(tag, "Network error enrichment fixtureId=$fixtureId message=${io.message}")
            DataResult.Error(ErrorType.NETWORK, io.message)
        } catch (e: Exception) {
            Log.e(tag, "Unknown enrichment error fixtureId=$fixtureId message=${e.message}", e)
            DataResult.Error(ErrorType.ENRICHMENT_FAIL, e.message)
        }
    }

    private suspend fun resolveEspnEventId(core: StatisticsResponse, fixtureId: Int, leagueSlug: String): String? {
        // 1) FAST PATH: trusted bridge mapping if available.
        val bridge = dao.getIdBridgeByType(fixtureId, "MATCH")
        if (bridge?.espnId != null) {
            Log.d(tag, "Id bridge hit fixtureId=$fixtureId espnId=${bridge.espnId}")
            return bridge.espnId
        }

        // 2) FALLBACK: resolve from scoreboard with date window (+/- 1 day).
        val primaryDate = toEspnDate(core.utcDate) ?: return null
        val datesToTry = listOf(primaryDate, shiftDate(primaryDate, -1), shiftDate(primaryDate, 1))
        val homeName = normalize(core.homeTeam?.name)
        val awayName = normalize(core.awayTeam?.name)
        Log.d(tag, "Matching teams home=$homeName away=$awayName")

        var matchedEventId: String? = null
        for (date in datesToTry) {
            Log.d(tag, "Resolving ESPN eventId via scoreboard league=$leagueSlug date=$date")
            val board = RetryPolicy.executeWithBackoff { espnApi.getScoreboard(leagueSlug, date) }
            Log.d(tag, "Scoreboard events returned=${board.events?.size ?: 0}")
            matchedEventId = findMatchInEvents(board.events ?: emptyList(), homeName, awayName)
            if (matchedEventId != null) {
                Log.d(tag, "Matched ESPN eventId=$matchedEventId on date=$date")
                break
            }
        }

        if (matchedEventId != null) {
            Log.d(tag, "Resolved ESPN eventId=$matchedEventId for fixtureId=$fixtureId")
            dao.insertIdBridge(
                IdBridgeEntity(
                    footballDataId = fixtureId,
                    entityType = "MATCH",
                    espnId = matchedEventId,
                    sportsDbId = null,
                    canonicalName = "${core.homeTeam?.name}_${core.awayTeam?.name}",
                    resolvedAt = System.currentTimeMillis()
                )
            )
        }

        if (matchedEventId == null) {
            Log.w(tag, "Failed to resolve ESPN eventId for fixtureId=$fixtureId")
        }
        return matchedEventId
    }

    private fun findMatchInEvents(events: List<JsonObject>, homeName: String, awayName: String): String? {
        val homeTokens = homeName.split(" ").filter { it.isNotBlank() }.toSet()
        val awayTokens = awayName.split(" ").filter { it.isNotBlank() }.toSet()

        return events.firstOrNull { event ->
            val comps = event.getAsJsonArray("competitions")?.firstOrNull()?.asJsonObject
                ?.getAsJsonArray("competitors")
            if (comps == null || comps.size() < 2) return@firstOrNull false

            val first = comps[0].asJsonObject.getAsJsonObject("team")
            val second = comps[1].asJsonObject.getAsJsonObject("team")
            val firstName = normalize(first?.get("displayName")?.asString)
            val secondName = normalize(second?.get("displayName")?.asString)

            val exact = (firstName == homeName && secondName == awayName) ||
                (firstName == awayName && secondName == homeName)
            if (exact) return@firstOrNull true

            val firstTokens = firstName.split(" ").filter { it.isNotBlank() }.toSet()
            val secondTokens = secondName.split(" ").filter { it.isNotBlank() }.toSet()
            val overlapForward = homeTokens.intersect(firstTokens).size >= 2 && awayTokens.intersect(secondTokens).size >= 2
            val overlapReverse = homeTokens.intersect(secondTokens).size >= 2 && awayTokens.intersect(firstTokens).size >= 2
            overlapForward || overlapReverse
        }?.get("id")?.asString
    }

    private fun mapSummaryToDetail(summary: com.example.soccerworld.data.remote.espn.dto.EspnSummaryDto, eventId: String): EspnMatchDetail {
        val detailsArray = summary.details ?: emptyList()
        val detailsEvents = detailsArray.map { detail ->
            EspnMatchEvent(
                minute = detail.getAsJsonObject("clock")?.get("displayValue")?.asString
                    ?: detail.get("clock")?.asString
                    ?: "--",
                type = detail.get("type")?.asJsonObject?.get("text")?.asString ?: "Event",
                description = detail.get("text")?.asString ?: "",
                team = detail.get("team")?.asJsonObject?.get("displayName")?.asString
            )
        }
        val fallbackEvents = buildFallbackEventsFromRosters(summary.rosters ?: emptyList())
        val events = if (detailsEvents.isNotEmpty()) {
            detailsEvents
        } else {
            Log.d(tag, "Top-level details empty, deriving from Roster Plays")
            fallbackEvents
        }

        val teamsStats = summary.boxscore
            ?.getAsJsonArray("teams")
            ?: JsonArray()
        val homeStats = teamsStats.firstOrNull { it.asJsonObject.get("homeAway")?.asString == "home" }
            ?.asJsonObject
            ?.getAsJsonArray("statistics")
            ?: JsonArray()
        val awayStats = teamsStats.firstOrNull { it.asJsonObject.get("homeAway")?.asString == "away" }
            ?.asJsonObject
            ?.getAsJsonArray("statistics")
            ?: JsonArray()

        val awayByName = mutableMapOf<String, String>()
        awayStats.forEach { stat ->
            val obj = stat.asJsonObject
            awayByName[obj.get("name")?.asString ?: ""] = obj.get("displayValue")?.asString ?: "-"
        }
        val stats = homeStats.map { stat ->
            val obj = stat.asJsonObject
            val name = obj.get("displayName")?.asString ?: obj.get("name")?.asString ?: "Stat"
            val rawName = obj.get("name")?.asString ?: ""
            EspnStatItem(
                name = name,
                homeValue = obj.get("displayValue")?.asString ?: "-",
                awayValue = awayByName[rawName] ?: "-"
            )
        }

        val lineupSource = summary.lineups ?: summary.rosters ?: emptyList()
        val lineups = lineupSource.map { roster ->
            val teamName = roster.getAsJsonObject("team")?.get("displayName")?.asString ?: "Team"
            val athletes = roster.getAsJsonArray("roster")
                ?: roster.getAsJsonArray("athletes")
                ?: JsonArray()
            val starters = mutableListOf<EspnLineupPlayer>()
            val substitutes = mutableListOf<EspnLineupPlayer>()
            athletes.forEach { a ->
                val obj = a.asJsonObject
                val athleteObj = obj.getAsJsonObject("athlete") ?: obj
                val player = EspnLineupPlayer(
                    name = athleteObj.get("displayName")?.asString ?: athleteObj.get("fullName")?.asString ?: "Player",
                    position = athleteObj.getAsJsonObject("position")?.get("abbreviation")?.asString
                        ?: obj.getAsJsonObject("position")?.get("abbreviation")?.asString,
                    imageUrl = athleteObj.getAsJsonObject("headshot")?.get("href")?.asString
                )
                if (obj.get("starter")?.asBoolean == true) starters.add(player) else substitutes.add(player)
            }
            EspnLineupTeam(
                teamName = teamName,
                formation = roster.get("formation")?.asString,
                starters = starters,
                substitutes = substitutes
            )
        }

        val venue = summary.gameInfo?.getAsJsonObject("venue")?.get("fullName")?.asString
        val status = summary.header
            ?.getAsJsonArray("competitions")
            ?.firstOrNull()
            ?.asJsonObject
            ?.getAsJsonObject("status")
            ?.get("type")
            ?.asJsonObject
            ?.get("name")
            ?.asString

        return EspnMatchDetail(
            eventId = eventId,
            venue = venue,
            events = events,
            stats = stats,
            lineups = lineups,
            status = status,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun buildFallbackEventsFromRosters(rosters: List<JsonObject>): List<EspnMatchEvent> {
        val events = mutableListOf<EspnMatchEvent>()
        rosters.forEach { roster ->
            val teamName = roster.getAsJsonObject("team")?.get("displayName")?.asString
            val players = roster.getAsJsonArray("roster") ?: JsonArray()
            players.forEach { p ->
                val playerObj = p.asJsonObject
                val playerName = playerObj.getAsJsonObject("athlete")?.get("displayName")?.asString ?: "Player"
                val plays = playerObj.getAsJsonArray("plays") ?: JsonArray()
                plays.forEach { playEl ->
                    val play = playEl.asJsonObject
                    val minute = play.getAsJsonObject("clock")?.get("displayValue")?.asString ?: "--"
                    val type = when {
                        play.get("scoringPlay")?.asBoolean == true -> "Goal"
                        play.get("redCard")?.asBoolean == true -> "Red Card"
                        play.get("yellowCard")?.asBoolean == true -> "Yellow Card"
                        play.get("substitution")?.asBoolean == true -> "Substitution"
                        else -> "Event"
                    }
                    events.add(
                        EspnMatchEvent(
                            minute = minute,
                            type = type,
                            description = "$playerName - $type",
                            team = teamName
                        )
                    )
                }
            }
        }
        return events.sortedBy { parseMinute(it.minute) }
    }

    private fun parseMinute(minute: String): Int {
        return minute.filter { it.isDigit() }.toIntOrNull() ?: Int.MAX_VALUE
    }

    private fun parseDetailFromEntity(entity: EspnMatchEnrichmentEntity): EspnMatchDetail? {
        return runCatching {
            val summary = gson.fromJson(entity.payloadJson, com.example.soccerworld.data.remote.espn.dto.EspnSummaryDto::class.java)
            mapSummaryToDetail(summary, entity.espnEventId)
        }.onFailure {
            Log.e(tag, "Failed to parse cached enrichment fixtureId=${entity.footballDataMatchId}: ${it.message}")
        }.getOrNull()
    }

    private fun toEspnDate(utcDate: String?): String? {
        if (utcDate.isNullOrBlank()) return null
        return runCatching {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date: Date = parser.parse(utcDate) ?: return null
            SimpleDateFormat("yyyyMMdd", Locale.US).format(date)
        }.getOrNull()
    }

    private fun normalize(name: String?): String {
        if (name == null) return ""

        val lower = name.lowercase(Locale.getDefault())
        val decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD)
        val accentFree = decomposed.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")

        val normalized = accentFree
            .replace("fc", "")
            .replace("cf", "")
            .replace("afc", "")
            .replace("club", "")
            .replace("\\sde\\s".toRegex(), " ")
            .replace("\\se\\s".toRegex(), " ")
            .replace("[^a-zA-Z0-9\\s]".toRegex(), "")
            .replace("\\s+".toRegex(), " ")
            .trim()

        return teamAliases[normalized] ?: normalized
    }

    private fun shiftDate(dateStr: String, days: Int): String {
        return runCatching {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
            val date = sdf.parse(dateStr) ?: return dateStr
            val cal = java.util.Calendar.getInstance().apply {
                time = date
                add(java.util.Calendar.DATE, days)
            }
            sdf.format(cal.time)
        }.getOrDefault(dateStr)
    }

    private fun JsonObject?.getAsJsonArray(name: String): JsonArray? {
        if (this == null || !has(name) || get(name).isJsonNull) return null
        return get(name).asJsonArray
    }

    private fun JsonArray.firstOrNull(): com.google.gson.JsonElement? = if (size() > 0) get(0) else null
}
