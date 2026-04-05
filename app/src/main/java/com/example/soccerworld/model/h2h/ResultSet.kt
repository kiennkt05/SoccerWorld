package com.example.soccerworld.model.h2h


import com.google.gson.annotations.SerializedName

data class ResultSet(
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("competitions")
    val competitions: String? = null,
    @SerializedName("first")
    val first: String? = null,
    @SerializedName("last")
    val last: String? = null
)