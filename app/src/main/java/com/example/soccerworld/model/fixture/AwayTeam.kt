package com.example.soccerworld.model.fixture


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AwayTeam(
    @SerializedName("team_id")
    var teamId: Int,
    @SerializedName("team_name")
    var teamName: String,
    @SerializedName("logo")
    var logo: String
) : Parcelable