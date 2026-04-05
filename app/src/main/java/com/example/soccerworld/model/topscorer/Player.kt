package com.example.soccerworld.model.topscorer


import com.google.gson.annotations.SerializedName

data class Player(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,
    @SerializedName("nationality")
    val nationality: String? = null,
    @SerializedName("section")
    val section: String? = null,
    @SerializedName("position")
    val position: Any? = null,
    @SerializedName("shirtNumber")
    val shirtNumber: Int? = null,
    @SerializedName("lastUpdated")
    val lastUpdated: String? = null
)