package com.example.soccerworld.model.transfer


import com.google.gson.annotations.SerializedName

data class Transfer(
    @SerializedName("player_id")
    var playerId: Int,
    @SerializedName("player_name")
    var playerName: String,
    @SerializedName("transfer_date")
    var transferDate: String,
    @SerializedName("type")
    var type: String,
    @SerializedName("team_in")
    var teamIn: TeamIn,
    @SerializedName("team_out")
    var teamOut: TeamOut,
    @SerializedName("lastUpdate")
    var lastUpdate: Int
)