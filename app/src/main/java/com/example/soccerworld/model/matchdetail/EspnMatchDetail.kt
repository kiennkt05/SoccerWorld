package com.example.soccerworld.model.matchdetail

data class EspnMatchEvent(
    val minute: String,
    val type: String,
    val description: String,
    val team: String?
)

data class EspnStatItem(
    val name: String,
    val homeValue: String,
    val awayValue: String
)

data class EspnLineupPlayer(
    val name: String,
    val position: String?,
    val imageUrl: String?
)

data class EspnLineupTeam(
    val teamName: String,
    val formation: String?,
    val starters: List<EspnLineupPlayer>,
    val substitutes: List<EspnLineupPlayer>
)

data class EspnMatchDetail(
    val eventId: String,
    val venue: String?,
    val events: List<EspnMatchEvent>,
    val stats: List<EspnStatItem>,
    val lineups: List<EspnLineupTeam>,
    val status: String?,
    val lastUpdated: Long
)
