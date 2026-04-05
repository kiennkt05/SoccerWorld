package com.example.soccerworld.model.h2h


import com.google.gson.annotations.SerializedName

data class Winner(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("shortName")
    val shortName: String? = null,
    @SerializedName("tla")
    val tla: String? = null,
    @SerializedName("crest")
    val crest: String? = null,
    @SerializedName("address")
    val address: String? = null,
    @SerializedName("website")
    val website: String? = null,
    @SerializedName("founded")
    val founded: Int? = null,
    @SerializedName("clubColors")
    val clubColors: String? = null,
    @SerializedName("venue")
    val venue: String? = null,
    @SerializedName("lastUpdated")
    val lastUpdated: String? = null
)