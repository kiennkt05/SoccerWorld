package com.example.soccerworld.model.fixture


import com.google.gson.annotations.SerializedName

data class FixtureResponse(
    @SerializedName("filters")
    val filters: Filters? = Filters(),
    @SerializedName("resultSet")
    val resultSet: ResultSet? = ResultSet(),
    @SerializedName("competition")
    val competition: Competition? = Competition(),
    @SerializedName("matches")
    val matches: List<Matche>? = listOf()
)