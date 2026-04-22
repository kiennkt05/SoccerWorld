package com.example.soccerworld.data.remote.sportsdb.dto

import com.google.gson.annotations.SerializedName

data class TeamMediaDto(
    @SerializedName("idTeam")
    val idTeam: String? = null,
    @SerializedName("strTeam")
    val strTeam: String? = null,
    @SerializedName("strBadge")
    val strBadge: String? = null,
    @SerializedName("strBanner")
    val strBanner: String? = null
)
