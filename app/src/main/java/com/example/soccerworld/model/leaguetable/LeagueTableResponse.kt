package com.example.soccerworld.model.leaguetable


import com.google.gson.annotations.SerializedName

data class LeagueTableResponse(
    @SerializedName("filters")
    val filters: Filters? = null,
    @SerializedName("area")
    val area: Area? = null,
    @SerializedName("competition")
    val competition: Competition? = null,
    @SerializedName("season")
    val season: Season? = null,
    @SerializedName("standings")
    val standings: List<Standing?>? = null
)