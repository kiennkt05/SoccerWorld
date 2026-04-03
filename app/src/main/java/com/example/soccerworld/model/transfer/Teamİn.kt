package com.example.soccerworld.model.transfer


import com.google.gson.annotations.SerializedName

data class TeamIn(
    @SerializedName("team_id")
    var teamId: Int,
    @SerializedName("team_name")
    var teamName: String
)