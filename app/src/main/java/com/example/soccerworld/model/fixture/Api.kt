package com.example.soccerworld.model.fixture


import com.google.gson.annotations.SerializedName

data class Api(
    @SerializedName("results")
    var results: Int,
    @SerializedName("fixtures")
    var fixtures: List<Fixture>
)