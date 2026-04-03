package com.example.soccerworld.model.team


import com.google.gson.annotations.SerializedName

data class Api(
    @SerializedName("results")
    var results: Int,
    @SerializedName("teams")
    var teams: List<Team>
)