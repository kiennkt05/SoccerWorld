package com.example.soccerworld.data.agent

import com.example.soccerworld.data.model.GroqFunctionDef
import com.example.soccerworld.data.model.GroqParameterSchema
import com.example.soccerworld.data.model.GroqPropertySchema
import com.example.soccerworld.data.model.GroqToolDefinition
import com.example.soccerworld.util.Constant

object ToolRegistry {
    fun allTools(): List<GroqToolDefinition> {
        val leagueIds = Constant.FLASHLIVE_LEAGUES.keys.toList()
        return listOf(
            buildTool(
                name = "search_team_or_player",
                description = "Tìm kiếm đội bóng hoặc cầu thủ theo tên",
                properties = mapOf("query" to GroqPropertySchema("string", "Tên đội/cầu thủ cần tìm")),
                required = listOf("query")
            ),
            buildTool(
                name = "get_league_standings",
                description = "Lấy bảng xếp hạng giải đấu",
                properties = mapOf("league_id" to GroqPropertySchema("string", "Mã giải", enum = leagueIds)),
                required = listOf("league_id")
            ),
            buildTool(
                name = "get_fixtures",
                description = "Lấy lịch thi đấu của giải",
                properties = mapOf(
                    "league_id" to GroqPropertySchema("string", "Mã giải", enum = leagueIds),
                    "status" to GroqPropertySchema("string", "Trạng thái trận (SCHEDULED, IN_PLAY, FINISHED)")
                ),
                required = listOf("league_id")
            ),
            buildTool(
                name = "get_head_to_head",
                description = "Lấy lịch sử đối đầu giữa 2 đội",
                properties = mapOf("fixture_id" to GroqPropertySchema("string", "ID của trận đấu")),
                required = listOf("fixture_id")
            ),
            buildTool(
                name = "get_match_detail",
                description = "Lấy chi tiết và thống kê trận đấu",
                properties = mapOf("fixture_id" to GroqPropertySchema("string", "ID của trận đấu")),
                required = listOf("fixture_id")
            ),
            buildTool(
                name = "get_team_players",
                description = "Lấy danh sách cầu thủ của đội",
                properties = mapOf("team_id" to GroqPropertySchema("string", "ID của đội bóng")),
                required = listOf("team_id")
            ),
            buildTool(
                name = "get_top_scorers",
                description = "Lấy danh sách vua phá lưới của giải",
                properties = mapOf("league_id" to GroqPropertySchema("string", "Mã giải", enum = leagueIds)),
                required = listOf("league_id")
            ),
            buildTool(
                name = "get_team_transfers",
                description = "Lấy thông tin chuyển nhượng của đội",
                properties = mapOf("team_id" to GroqPropertySchema("string", "ID của đội bóng")),
                required = listOf("team_id")
            ),
            buildTool(
                name = "get_team_matches",
                description = "Lấy các trận đấu gần đây của đội bóng",
                properties = mapOf(
                    "team_id" to GroqPropertySchema("string", "ID của đội bóng"),
                    "is_results" to GroqPropertySchema("string", "Loại dữ liệu cần lấy", enum = listOf("results", "fixtures"))
                ),
                required = listOf("team_id")
            )
        )
    }

    private fun buildTool(name: String, description: String, properties: Map<String, GroqPropertySchema>, required: List<String>): GroqToolDefinition {
        return GroqToolDefinition(
            function = GroqFunctionDef(
                name = name,
                description = description,
                parameters = GroqParameterSchema(properties = properties, required = required)
            )
        )
    }
}
