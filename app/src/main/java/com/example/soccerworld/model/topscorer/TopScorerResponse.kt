package com.example.soccerworld.model.topscorer


import com.google.gson.annotations.SerializedName

data class TopScorerResponse(
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("filters")
    val filters: Filters? = null,
    @SerializedName("competition")
    val competition: Competition? = null,
    @SerializedName("season")
    val season: Season? = null,
    @SerializedName("scorers")
    val scorers: List<Scorer?>? = null
)