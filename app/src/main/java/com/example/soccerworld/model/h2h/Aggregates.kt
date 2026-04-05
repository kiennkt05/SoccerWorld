package com.example.soccerworld.model.h2h


import com.google.gson.annotations.SerializedName

data class Aggregates(
    @SerializedName("numberOfMatches")
    val numberOfMatches: Int? = null,
    @SerializedName("totalGoals")
    val totalGoals: Int? = null,
    @SerializedName("homeTeam")
    val homeTeam: HomeTeam? = null,
    @SerializedName("awayTeam")
    val awayTeam: AwayTeam? = null
)