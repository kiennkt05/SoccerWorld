package com.example.soccerworld.model.h2h


import com.google.gson.annotations.SerializedName

data class Team(
    @SerializedName("team_id")
    var teamId: Int,
    @SerializedName("team_name")
    var teamName: String,
    @SerializedName("team_logo")
    var teamLogo: String,
    @SerializedName("statistics")
    var statistics: Statistics
)