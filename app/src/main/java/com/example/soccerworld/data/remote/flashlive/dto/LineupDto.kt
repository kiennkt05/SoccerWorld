package com.example.soccerworld.data.remote.flashlive.dto

import com.google.gson.annotations.SerializedName

data class LineupResponse(
    @SerializedName("DATA") val data: List<LineupGroup>
)

data class LineupGroup(
    @SerializedName("FORMATION_NAME") val formationName: String, 
    @SerializedName("FORMATIONS") val formations: List<Formation>,
    @SerializedName("PLAYER_GROUP_TYPE") val playerGroupType: Int? = null
)

data class Formation(
    @SerializedName("FORMATION_LINE") val teamSide: Int, // 1 = Home, 2 = Away
    @SerializedName("FORMATION_DISPOSTION") val disposition: String,
    @SerializedName("MEMBERS") val members: List<LineupPlayer>
)

data class LineupPlayer(
    @SerializedName("PLAYER_ID") val id: String,
    @SerializedName("PLAYER_FULL_NAME") val fullName: String,
    @SerializedName("SHORT_NAME") val shortName: String,
    @SerializedName("PLAYER_NUMBER") val number: Int? = null,
    @SerializedName("LPR") val rating: String? = null, // Sofascore rating
    @SerializedName("LPI") val imageId: String? = null, // Player Image
    @SerializedName("INCIDENTS") val incidents: List<Int>? = null,
    @SerializedName("PLAYER_POSITION") val position: Int? = 0 // Required for sorting
)
