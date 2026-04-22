package com.example.soccerworld.model.player


import com.google.gson.annotations.SerializedName

data class Squad(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("position")
    val position: String? = null,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    @SerializedName("nationality")
    val nationality: String? = null,
    val imageUrl: String? = null
)