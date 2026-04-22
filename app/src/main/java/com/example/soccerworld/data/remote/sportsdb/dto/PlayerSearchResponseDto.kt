package com.example.soccerworld.data.remote.sportsdb.dto

import com.google.gson.annotations.SerializedName

data class PlayerSearchResponseDto(
    @SerializedName("player")
    val player: List<PlayerMediaDto>? = null
)
