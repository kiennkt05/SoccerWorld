package com.example.soccerworld.model.h2h


import com.google.gson.annotations.SerializedName

data class Score(
    @SerializedName("winner")
    val winner: String? = null,
    @SerializedName("duration")
    val duration: String? = null,
    @SerializedName("fullTime")
    val fullTime: FullTime? = null,
    @SerializedName("halfTime")
    val halfTime: HalfTime? = null
)