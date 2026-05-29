package com.example.soccerworld.util

data class FlashLiveLeague(
    val stageId: String,
    val seasonId: String? = null,
    val name: String,
    val additionalStageIds: List<String> = emptyList(),
    val countryName: String? = null,
    val image: String? = null
) {
    val allStageIds: List<String>
        get() = listOf(stageId) + additionalStageIds
}

object Constant {
    const val LOCALE = "en_INT"
    const val SPORT_ID = 1
    const val TIMEZONE = 0
    const val DAYS_TO_UPDATE = 3

    val FLASHLIVE_LEAGUES = mapOf(
        "PL" to FlashLiveLeague("OEEq9Yvp", "KKay4EE8", "Premier League", countryName = "England", image = "https://crests.football-data.org/PL.png"),
        "PD" to FlashLiveLeague("vcm2MhGk", "UkksTK1s", "LaLiga", countryName = "Spain", image = "https://crests.football-data.org/PD.png"),
        "BL1" to FlashLiveLeague("8UYeqfiD", "QwzghtID", "Bundesliga", countryName = "Germany", image = "https://crests.football-data.org/BL1.png"),
        "SA" to FlashLiveLeague("6PWwAsA7", "04lKZTBr", "Serie A", countryName = "Italy", image = "https://crests.football-data.org/SA.png"),
        "FL1" to FlashLiveLeague("j9QeTLPP", "hnFBS5hK", "Ligue 1", countryName = "France", image = "https://crests.football-data.org/FL1.png"),
        "CL" to FlashLiveLeague("UiRZST3U", "bLJeeS2d", "UEFA Champions League", listOf("lMPimXln", "AVQmlDZu"), countryName = "Europe", image = "https://crests.football-data.org/CL.png"),
        "EL" to FlashLiveLeague("AZYgKOBc", "ns2vvTIF", "UEFA Europa League", listOf("61S2I2tA", "t6Dr0fQr"), countryName = "Europe", image = "https://www.flashscore.com/res/image/data/lxUusmmd-nyYh4pi6.png"),
        "ECL" to FlashLiveLeague("plJ0oBJb", "4rMnKBuc", "UEFA Conference League", listOf("dWpHqklB", "vBI4pVY4"), countryName = "Europe", image = "https://www.flashscore.com/res/image/data/6a9THkA7-fmGCnzrf.png"),
        "WC" to FlashLiveLeague("SbLsX4y7", "zeSHfCx3", "FIFA World Cup", listOf("6kKoWOjD", "jV1yMmNl", "r96vrVq8", "Y5chSkpL", "d82XMT7r", "ncepKRi1", "xASUZ6il", "Y11nB0rh", "QmflJo77", "f15Izmxr", "SKddnQLm", "UL0uL7xe", "OIEnt9EK", "EDGLhSrK", "8zGPinbQ", "KYHZYQ6f", "GUncR9aR", "YyMwYpM0"), countryName = "World", image = "https://crests.football-data.org/qatar.png"),
        "CWC" to FlashLiveLeague("KOtwQCtI", "0bQaeJD7", "Club World Cup", listOf("QNUibvm2", "tvVoOjBU"), countryName = "World", image = "https://www.flashscore.com/res/image/data/GYrv8eC7-lAqBEeCk.png"),
        "FCC" to FlashLiveLeague("AL0d1hfp", "dvflUV3a", "FIFA Confederations Cup", listOf("Me400C9j"), countryName = "World", image = "https://www.flashscore.com/res/image/data/rkwbpoV1-O4g3SEWt.png")
    )

    fun league(leagueCode: String): FlashLiveLeague? = FLASHLIVE_LEAGUES[leagueCode]

    const val TEAM_ID = "team_id"
    const val FIXTURE_TEAM_IDS = "h2h_team_ids"
}