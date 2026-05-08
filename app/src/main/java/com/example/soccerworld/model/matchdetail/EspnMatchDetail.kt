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
    val shortName: String,
    val position: String?,
    val imageUrl: String?,
    val number: Int? = null,
    val rating: String? = null,
    val incidents: List<Int>? = null,
    val isCaptain: Boolean = false,
    val fieldPosition: Int? = null
)

data class MatchLineupTeam(
    val teamName: String,
    val formation: String?,
    val averageRating: Double?,
    val starters: List<MatchLineupPlayer>,
    val substitutes: List<MatchLineupPlayer>,
    val coach: MatchLineupPlayer? = null
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
