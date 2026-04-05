package com.example.soccerworld.model.statistic


import com.google.gson.annotations.SerializedName

data class StatisticsResponse(
    @SerializedName("area")
    val area: Area? = null,
    @SerializedName("competition")
    val competition: Competition? = null,
    @SerializedName("season")
    val season: Season? = null,
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("utcDate")
    val utcDate: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("venue")
    val venue: Any? = null,
    @SerializedName("matchday")
    val matchday: Int? = null,
    @SerializedName("stage")
    val stage: String? = null,
    @SerializedName("group")
    val group: Any? = null,
    @SerializedName("lastUpdated")
    val lastUpdated: String? = null,
    @SerializedName("homeTeam")
    val homeTeam: HomeTeam? = null,
    @SerializedName("awayTeam")
    val awayTeam: AwayTeam? = null,
    @SerializedName("score")
    val score: Score? = null,
    @SerializedName("odds")
    val odds: Odds? = null,
    @SerializedName("referees")
    val referees: List<Referee?>? = null
)