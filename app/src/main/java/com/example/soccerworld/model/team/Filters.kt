package com.example.soccerworld.model.team


import com.google.gson.annotations.SerializedName

data class Filters(
    @SerializedName("season")
    val season: String? = null
)