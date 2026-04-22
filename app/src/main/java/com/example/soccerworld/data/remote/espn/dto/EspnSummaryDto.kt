package com.example.soccerworld.data.remote.espn.dto

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class EspnSummaryDto(
    @SerializedName("header")
    val header: JsonObject? = null,
    @SerializedName("drives")
    val drives: JsonObject? = null,
    @SerializedName("boxscore")
    val boxscore: JsonObject? = null,
    @SerializedName("gameInfo")
    val gameInfo: JsonObject? = null,
    @SerializedName("rosters")
    val rosters: List<JsonObject>? = null,
    @SerializedName("details")
    val details: List<JsonObject>? = null
    ,
    @SerializedName("lineups")
    val lineups: List<JsonObject>? = null
)
