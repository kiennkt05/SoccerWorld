package com.example.soccerworld.model.team


import com.google.gson.annotations.SerializedName

data class TeamResponse(
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("filters")
    val filters: Filters? = null,
    @SerializedName("competition")
    val competition: Competition? = null,
    @SerializedName("season")
    val season: Season? = null,
    @SerializedName("teams")
    val teams: List<Team?>? = null
)