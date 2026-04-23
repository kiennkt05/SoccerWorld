package com.example.soccerworld.model.h2h


import com.google.gson.annotations.SerializedName

data class Matche(
    @SerializedName("area")
    val area: Area? = Area(),
    @SerializedName("competition")
    val competition: Competition? = Competition(),
    @SerializedName("season")
    val season: Season? = Season(),
    @SerializedName("id")
    val id: String? = "",
    @SerializedName("utcDate")
    val utcDate: String? = "",
    @SerializedName("status")
    val status: String? = "",
    @SerializedName("matchday")
    val matchday: Int? = 0,
    @SerializedName("stage")
    val stage: String? = "",
    @SerializedName("group")
    val group: Any? = Any(),
    @SerializedName("lastUpdated")
    val lastUpdated: String? = "",
    @SerializedName("homeTeam")
    val homeTeam: HomeTeamX? = HomeTeamX(),
    @SerializedName("awayTeam")
    val awayTeam: AwayTeamX? = AwayTeamX(),
    @SerializedName("score")
    val score: Score? = Score(),
    @SerializedName("odds")
    val odds: Odds? = Odds(),
    @SerializedName("referees")
    val referees: List<Referee>? = listOf()
)