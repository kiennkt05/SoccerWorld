package com.example.soccerworld.model.fixture


import com.google.gson.annotations.SerializedName

data class ResultSet(
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("first")
    val first: String? = null,
    @SerializedName("last")
    val last: String? = null,
    @SerializedName("played")
    val played: Int? = null
)