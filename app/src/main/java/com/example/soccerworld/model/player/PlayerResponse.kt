package com.example.soccerworld.model.player


import com.google.gson.annotations.SerializedName

data class PlayerResponse(
    @SerializedName("api")
    var api: Api
)