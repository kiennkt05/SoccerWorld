package com.example.soccerworld.model.topscorer


import com.google.gson.annotations.SerializedName

data class Filters(
    @SerializedName("season")
    val season: String? = null,
    @SerializedName("limit")
    val limit: Int? = null
)