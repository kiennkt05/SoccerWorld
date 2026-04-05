package com.example.soccerworld.model.statistic


import com.google.gson.annotations.SerializedName

data class FullTime(
    @SerializedName("home")
    val home: Int? = null,
    @SerializedName("away")
    val away: Int? = null
)