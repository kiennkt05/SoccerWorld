package com.example.soccerworld.model.fixture


import com.google.gson.annotations.SerializedName

data class FullTime(
    @SerializedName("home")
    val home: Int? = null,
    @SerializedName("away")
    val away: Int? = null
)