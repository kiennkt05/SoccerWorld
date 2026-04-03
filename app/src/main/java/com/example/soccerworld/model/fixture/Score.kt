package com.example.soccerworld.model.fixture


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Score(
    @SerializedName("halftime")
    var halftime: String?,
    @SerializedName("fulltime")
    var fulltime: String?,
    @SerializedName("extratime")
    var extratime: @RawValue Any?,
    @SerializedName("penalty")
    var penalty: @RawValue Any?
) : Parcelable