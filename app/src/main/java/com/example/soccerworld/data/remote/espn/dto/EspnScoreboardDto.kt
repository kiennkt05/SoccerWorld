package com.example.soccerworld.data.remote.espn.dto

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class EspnScoreboardDto(
    @SerializedName("events")
    val events: List<JsonObject>? = null
)
