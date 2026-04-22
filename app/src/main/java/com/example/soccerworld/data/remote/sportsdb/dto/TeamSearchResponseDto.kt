package com.example.soccerworld.data.remote.sportsdb.dto

import com.google.gson.annotations.SerializedName

data class TeamSearchResponseDto(
    @SerializedName("teams")
    val teams: List<TeamMediaDto>? = null
)
