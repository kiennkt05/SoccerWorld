package com.example.soccerworld.model.leaguetable


import com.google.gson.annotations.SerializedName

data class Table(
    @SerializedName("position")
    val position: Int? = null,
    @SerializedName("team")
    val team: Team? = null,
    @SerializedName("playedGames")
    val playedGames: Int? = null,
    @SerializedName("form")
    val form: Any? = null,
    @SerializedName("won")
    val won: Int? = null,
    @SerializedName("draw")
    val draw: Int? = null,
    @SerializedName("lost")
    val lost: Int? = null,
    @SerializedName("points")
    val points: Int? = null,
    @SerializedName("goalsFor")
    val goalsFor: Int? = null,
    @SerializedName("goalsAgainst")
    val goalsAgainst: Int? = null,
    @SerializedName("goalDifference")
    val goalDifference: Int? = null
)