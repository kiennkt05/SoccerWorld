package com.example.soccerworld.model.leaguetable


import com.google.gson.annotations.SerializedName

data class Standing(
    @SerializedName("stage")
    val stage: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("group")
    val group: Any? = null,
    @SerializedName("table")
    val table: List<Table?>? = null
)