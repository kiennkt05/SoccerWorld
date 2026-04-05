package com.example.soccerworld.model.player


import com.google.gson.annotations.SerializedName

data class Coach(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    @SerializedName("nationality")
    val nationality: String? = null,
    @SerializedName("contract")
    val contract: Contract? = null
)