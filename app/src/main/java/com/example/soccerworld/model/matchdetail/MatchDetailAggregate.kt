package com.example.soccerworld.model.matchdetail

import com.example.soccerworld.model.h2h.Matche as H2HMatch
import com.example.soccerworld.model.statistic.StatisticsResponse

data class MatchDetailAggregate(
    val core: StatisticsResponse?,
    val h2h: List<H2HMatch>,
    val enrichment: MatchEnrichmentDetail?
)
