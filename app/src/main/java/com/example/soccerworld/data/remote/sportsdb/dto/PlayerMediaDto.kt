package com.example.soccerworld.data.remote.sportsdb.dto

import com.google.gson.annotations.SerializedName

data class PlayerMediaDto(
    @SerializedName("idPlayer")
    val idPlayer: String? = null,
    @SerializedName("strPlayer")
    val strPlayer: String? = null,
    @SerializedName("strThumb")
    val strThumb: String? = null,
    @SerializedName("strCutout")
    val strCutout: String? = null
)
