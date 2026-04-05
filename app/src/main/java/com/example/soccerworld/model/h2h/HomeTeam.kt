package com.example.soccerworld.model.h2h


import com.google.gson.annotations.SerializedName

data class HomeTeam(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("wins")
    val wins: Int? = null,
    @SerializedName("draws")
    val draws: Int? = null,
    @SerializedName("losses")
    val losses: Int? = null
)