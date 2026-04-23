package com.example.soccerworld.model.matchdetail

data class MatchEvent(
    val minute: String,
    val type: String,
    val description: String,
    val team: String?
)

data class MatchStatItem(
    val name: String,
    val homeValue: String,
    val awayValue: String
)

data class MatchLineupPlayer(
    val name: String,
    val position: String?,
    val imageUrl: String?
)

data class MatchLineupTeam(
    val teamName: String,
    val formation: String?,
    val starters: List<MatchLineupPlayer>,
    val substitutes: List<MatchLineupPlayer>
)

data class MatchEnrichmentDetail(
    val eventId: String,
    val venue: String?,
    val events: List<MatchEvent>,
    val stats: List<MatchStatItem>,
    val lineups: List<MatchLineupTeam>,
    val status: String?,
    val lastUpdated: Long
)
