package com.example.soccerworld.model.h2h


import com.google.gson.annotations.SerializedName

data class Filters(
    @SerializedName("limit")
    val limit: Int? = null,
    @SerializedName("permission")
    val permission: String? = null
)