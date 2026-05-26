package com.example.soccerworld.data.agent

import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.data.model.GroqToolCall
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class ToolExecutor(private val repository: FootballRepository) {
    private val gson = Gson()

    suspend fun execute(toolCall: GroqToolCall): String {
        return try {
            val args = gson.fromJson(toolCall.function.arguments, JsonObject::class.java)
            when (toolCall.function.name) {
                "search_team_or_player" -> {
                    val query = args.get("query")?.asString ?: return errorStr("Missing query")
                    formatResult(repository.multiSearch(query), maxArraySize = 5)
                }
                "get_league_standings" -> {
                    val leagueId = args.get("league_id")?.asString ?: return errorStr("Missing league_id")
                    formatResult(repository.getLeagueTable(leagueId), maxArraySize = 40)
                }
                "get_fixtures" -> {
                    val leagueId = args.get("league_id")?.asString ?: return errorStr("Missing league_id")
                    val status = args.get("status")?.asString
                    formatResult(repository.getAllFixtureOfLeague(leagueId, status = status), maxArraySize = 10)
                }
                "get_head_to_head" -> {
                    val fixtureId = args.get("fixture_id")?.asString ?: return errorStr("Missing fixture_id")
                    if (!isValidHashId(fixtureId)) return invalidIdError("fixture_id", fixtureId)
                    formatResult(repository.getAllH2hItems(fixtureId), maxArraySize = 8)
                }
                "get_match_detail" -> {
                    val fixtureId = args.get("fixture_id")?.asString ?: return errorStr("Missing fixture_id")
                    if (!isValidHashId(fixtureId)) return invalidIdError("fixture_id", fixtureId)
                    formatResult(repository.getMatchDetailAggregate(fixtureId), maxArraySize = 10)
                }
                "get_team_players" -> {
                    val teamId = args.get("team_id")?.asString ?: return errorStr("Missing team_id")
                    if (!isValidHashId(teamId)) return invalidIdError("team_id", teamId)
                    formatResult(repository.getAllPlayersOfTeam(teamId), maxArraySize = 25)
                }
                "get_top_scorers" -> {
                    val leagueId = args.get("league_id")?.asString ?: return errorStr("Missing league_id")
                    formatResult(repository.getTopScorers(leagueId), maxArraySize = 15)
                }
                "get_team_transfers" -> {
                    val teamId = args.get("team_id")?.asString ?: return errorStr("Missing team_id")
                    if (!isValidHashId(teamId)) return invalidIdError("team_id", teamId)
                    formatResult(repository.getTeamTransfers(teamId), maxArraySize = 15)
                }
                "get_team_matches" -> {
                    val teamId = args.get("team_id")?.asString ?: return errorStr("Missing team_id")
                    if (!isValidHashId(teamId)) return invalidIdError("team_id", teamId)
                    // is_results is now a string enum: "results" (default) | "fixtures"
                    val isResultsStr = args.get("is_results")?.asString ?: "results"
                    val isResults = isResultsStr != "fixtures"
                    formatResult(repository.getTeamMatches(teamId, 1, isResults), maxArraySize = 10)
                }
                else -> errorStr("Unknown tool: ${toolCall.function.name}")
            }
        } catch (e: Exception) {
            errorStr("Error executing tool: ${e.message}")
        }
    }

    private fun isValidHashId(id: String): Boolean {
        // FlashLive IDs are alphanumeric hashes of length exactly 8 (e.g., 'hA1Zm19f').
        if (id.length != 8) return false
        val isAlphanumeric = id.all { it.isLetterOrDigit() }
        if (!isAlphanumeric) return false
        
        // If the ID contains only letters (no digits), check if it looks like a standard plain name
        if (id.all { it.isLetter() }) {
            // Reject all uppercase names (e.g., "PSG", "BARCA")
            if (id.all { it.isUpperCase() }) return false
            
            // Reject all lowercase names (e.g., "arsenal", "chelsea")
            if (id.all { it.isLowerCase() }) return false
            
            // Reject standard Title Case names (e.g., "Arsenal", "Chelsea", "Liverpool")
            val first = id.firstOrNull() ?: return false
            val rest = id.drop(1)
            if (first.isUpperCase() && rest.all { it.isLowerCase() }) return false
        }
        
        return true
    }

    private fun invalidIdError(paramName: String, value: String): String {
        return """{"error": "Invalid $paramName format: '$value'. All team and match IDs in the system are unique alphanumeric hashes (e.g. 'hA1Zm19f'). You MUST call 'search_team_or_player' with query='$value' first to find the correct ID before calling this tool!"}"""
    }

    private fun <T> formatResult(result: DataResult<T>, maxArraySize: Int = 10): String {
        return when (result) {
            is DataResult.Success -> {
                val element = gson.toJsonTree(result.data)
                val cleanedElement = cleanJsonElement(element, maxArraySize)
                val jsonString = gson.toJson(cleanedElement)
                if (jsonString.length > 30000) jsonString.substring(0, 30000) + "...[TRUNCATED]" else jsonString
            }
            is DataResult.Error -> {
                val errorMsg = result.message.orEmpty()
                val descriptiveMsg = if (errorMsg.isBlank()) "API Error type: ${result.type}" else errorMsg
                errorStr(descriptiveMsg)
            }
            is DataResult.Loading -> errorStr("Loading...")
        }
    }

    private fun cleanJsonElement(element: JsonElement, maxArraySize: Int): JsonElement {
        if (element.isJsonObject) {
            val oldObj = element.asJsonObject
            val newObj = JsonObject()
            for (entry in oldObj.entrySet()) {
                val key = entry.key
                // Skip any keys containing images, crests, logos, flags, emblems
                val isImageKey = key.contains("image", ignoreCase = true) ||
                                 key.contains("crest", ignoreCase = true) ||
                                 key.contains("logo", ignoreCase = true) ||
                                 key.contains("flag", ignoreCase = true) ||
                                 key.contains("emblem", ignoreCase = true)
                if (!isImageKey) {
                    newObj.add(key, cleanJsonElement(entry.value, maxArraySize))
                }
            }
            return newObj
        } else if (element.isJsonArray) {
            val oldArr = element.asJsonArray
            val newArr = JsonArray()
            val limit = minOf(oldArr.size(), maxArraySize)
            for (i in 0 until limit) {
                newArr.add(cleanJsonElement(oldArr.get(i), maxArraySize))
            }
            return newArr
        }
        return element
    }

    private fun errorStr(msg: String) = """{"error": "$msg"}"""
}
