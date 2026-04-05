package com.example.soccerworld.model.topscorer


import com.google.gson.annotations.SerializedName

data class Scorer(
    @SerializedName("player")
    val player: Player? = null,
    @SerializedName("team")
    val team: Team? = null,
    @SerializedName("playedMatches")
    val playedMatches: Int? = null,
    @SerializedName("goals")
    val goals: Int? = null,
    @SerializedName("assists")
    val assists: Int? = null,
    @SerializedName("penalties")
    val penalties: Int? = null
)