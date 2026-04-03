package com.example.soccerworld.model.topscorer


import com.google.gson.annotations.SerializedName

data class Penalty(
    @SerializedName("won")
    var won: Int,

    @SerializedName("success")
    var success: Int,
    @SerializedName("missed")
    var missed: Int

)