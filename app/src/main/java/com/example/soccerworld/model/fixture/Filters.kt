package com.example.soccerworld.model.fixture


import com.google.gson.annotations.SerializedName

data class Filters(
    @SerializedName("season")
    val season: String? = null
)