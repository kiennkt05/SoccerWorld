package com.example.soccerworld.model.h2h


import com.google.gson.annotations.SerializedName

data class HalfTime(
    @SerializedName("home")
    val home: Int? = null,
    @SerializedName("away")
    val away: Int? = null
)