package com.example.soccerworld.model.team


import com.google.gson.annotations.SerializedName

data class Contract(
    @SerializedName("start")
    val start: String? = null,
    @SerializedName("until")
    val until: String? = null
)