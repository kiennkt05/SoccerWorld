package com.example.soccerworld.data.agent

import com.example.soccerworld.data.FootballRepository
import com.example.soccerworld.data.model.DataResult
import com.example.soccerworld.data.model.GroqToolCall
import com.google.gson.Gson
import com.google.gson.JsonObject

class ToolExecutor(private val repository: FootballRepository) {
    private val gson = Gson()

    suspend fun execute(toolCall: GroqToolCall): String {
        return try {
            val args = gson.fromJson(toolCall.function.arguments, JsonObject::class.java)
            when (toolCall.function.name) {
                "search_team_or_player" -> {
                    val query = args.get("query")?.asString ?: return errorStr("Missing query")
                    formatResult(repository.multiSearch(query))
                }
                "get_league_standings" -> {
                    val leagueId = args.get("league_id")?.asString ?: return errorStr("Missing league_id")
                    formatResult(repository.getLeagueTable(leagueId))
                }
                "get_fixtures" -> {
                    val leagueId = args.get("league_id")?.asString ?: return errorStr("Missing league_id")
                    val status = args.get("status")?.asString
                    formatResult(repository.getAllFixtureOfLeague(leagueId, status = status))
                }
                "get_head_to_head" -> {
                    val fixtureId = args.get("fixture_id")?.asString ?: return errorStr("Missing fixture_id")
                    formatResult(repository.getAllH2hItems(fixtureId))
                }
                "get_match_detail" -> {
                    val fixtureId = args.get("fixture_id")?.asString ?: return errorStr("Missing fixture_id")
                    formatResult(repository.getMatchDetailAggregate(fixtureId))
                }
                "get_team_players" -> {
                    val teamId = args.get("team_id")?.asString ?: return errorStr("Missing team_id")
                    formatResult(repository.getAllPlayersOfTeam(teamId))
                }
                "get_top_scorers" -> {
                    val leagueId = args.get("league_id")?.asString ?: return errorStr("Missing league_id")
                    formatResult(repository.getTopScorers(leagueId))
                }
                "get_team_transfers" -> {
                    val teamId = args.get("team_id")?.asString ?: return errorStr("Missing team_id")
                    formatResult(repository.getTeamTransfers(teamId))
                }
                "get_team_matches" -> {
                    val teamId = args.get("team_id")?.asString ?: return errorStr("Missing team_id")
                    // is_results is now a string enum: "results" (default) | "fixtures"
                    val isResultsStr = args.get("is_results")?.asString ?: "results"
                    val isResults = isResultsStr != "fixtures"
                    formatResult(repository.getTeamMatches(teamId, 1, isResults))
                }
                else -> errorStr("Unknown tool: ${toolCall.function.name}")
            }
        } catch (e: Exception) {
            errorStr("Error executing tool: ${e.message}")
        }
    }

    private fun <T> formatResult(result: DataResult<T>): String {
        return when (result) {
            is DataResult.Success -> {
                // Return truncated JSON to avoid exceeding token limit
                val fullJson = gson.toJson(result.data)
                if (fullJson.length > 30000) fullJson.substring(0, 30000) + "...[TRUNCATED]" else fullJson
            }
            is DataResult.Error -> errorStr(result.message ?: "Unknown error")
            is DataResult.Loading -> errorStr("Loading...")
        }
    }

    private fun errorStr(msg: String) = """{"error": "$msg"}"""
}
