package com.example.soccerworld.model.fixture


import com.google.gson.annotations.SerializedName

data class Referee(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("nationality")
    val nationality: String? = null
)