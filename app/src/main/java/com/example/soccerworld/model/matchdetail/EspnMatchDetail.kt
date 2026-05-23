package com.example.soccerworld.model.matchdetail

import com.example.soccerworld.data.remote.flashlive.EventStatsStage
import com.example.soccerworld.data.remote.flashlive.HighlightImage

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

data class MatchHighlight(
    val title: String,
    val link: String,
    val images: List<HighlightImage>? = null,
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

data class MatchNews(
    val id: String?,
    val title: String?,
    val link: String?,
    val published: Long?,
    val providerName: String?,
    val imageUrl: String?
)

data class MatchEnrichmentDetail(
    val eventId: String,
    val venue: String?,
    val events: List<MatchEvent>,
    val stats: List<MatchStatItem>,
    val statStages: List<EventStatsStage> = emptyList(),
    val highlights: List<MatchHighlight>,
    val lineups: List<MatchLineupTeam>,
    val news: List<MatchNews> = emptyList(),
    val status: String?,
    val lastUpdated: Long
)
