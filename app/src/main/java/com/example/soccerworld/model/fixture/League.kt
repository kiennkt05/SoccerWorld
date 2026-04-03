package com.example.soccerworld.model.fixture


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class League(
    @SerializedName("name")
    var name: String,
    @SerializedName("country")
    var country: String,
    @SerializedName("logo")
    var logo: String,
    @SerializedName("flag")
    var flag: String
) : Parcelable