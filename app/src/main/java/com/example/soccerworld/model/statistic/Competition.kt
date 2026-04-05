package com.example.soccerworld.model.statistic


import com.google.gson.annotations.SerializedName

data class Competition(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("emblem")
    val emblem: String? = null
)