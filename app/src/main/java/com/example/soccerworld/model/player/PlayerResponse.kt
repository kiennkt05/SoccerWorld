package com.example.soccerworld.model.player


import com.google.gson.annotations.SerializedName

data class PlayerResponse(
    @SerializedName("area")
    val area: Area? = null,
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
    @SerializedName("runningCompetitions")
    val runningCompetitions: List<RunningCompetition?>? = null,
    @SerializedName("coach")
    val coach: Coach? = null,
    @SerializedName("squad")
    val squad: List<Squad?>? = null,
    @SerializedName("staff")
    val staff: List<Any?>? = null,
    @SerializedName("lastUpdated")
    val lastUpdated: String? = null
)