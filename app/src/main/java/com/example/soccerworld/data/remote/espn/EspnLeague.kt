package com.example.soccerworld.data.remote.espn

enum class EspnLeague(val fdCode: String, val slug: String) {
    PREMIER_LEAGUE("PL", "eng.1"),
    LA_LIGA("PD", "esp.1"),
    BUNDESLIGA("BL1", "ger.1"),
    SERIE_A("SA", "ita.1"),
    LIGUE_1("FL1", "fra.1"),
    UCL("CL", "uefa.champions"),
    WORLD_CUP("WC", "fifa.world.cup");

    companion object {
        fun fromFdCode(fdCode: String?): EspnLeague? {
            if (fdCode.isNullOrBlank()) return null
            return entries.firstOrNull { it.fdCode == fdCode }
        }
    }
}
