package com.example.soccerworld.util

data class FlashLiveLeague(
    val stageId: String,
    val seasonId: String,
    val name: String
)

object Constant {
    const val LOCALE = "en_INT"
    const val SPORT_ID = 1
    const val TIMEZONE = 0
    const val DAYS_TO_UPDATE = 3

    val FLASHLIVE_LEAGUES = mapOf(
        "PL" to FlashLiveLeague("OEEq9Yvp", "KKay4EE8", "Premier League"),
        "PD" to FlashLiveLeague("vcm2MhGk", "UkksTK1s", "LaLiga"),
        "BL1" to FlashLiveLeague("8UYeqfiD", "QwzghtID", "Bundesliga"),
        "SA" to FlashLiveLeague("6PWwAsA7", "04lKZTBr", "Serie A"),
        "FL1" to FlashLiveLeague("j9QeTLPP", "hnFBS5hK", "Ligue 1"),
        "CL" to FlashLiveLeague("AVQmlDZu", "bLJeeS2d", "Champions League")
    )

    fun league(leagueCode: String): FlashLiveLeague? = FLASHLIVE_LEAGUES[leagueCode]

    const val TEAM_ID = "team_id"
    const val FIXTURE_TEAM_IDS = "h2h_team_ids"
}