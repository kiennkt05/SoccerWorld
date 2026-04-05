package com.example.soccerworld.model.h2h


import com.google.gson.annotations.SerializedName

data class Season(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("endDate")
    val endDate: String? = null,
    @SerializedName("currentMatchday")
    val currentMatchday: Int? = null,
    @SerializedName("winner")
    val winner: Winner? = null
)