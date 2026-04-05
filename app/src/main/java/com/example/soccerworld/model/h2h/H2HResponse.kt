package com.example.soccerworld.model.h2h


import com.google.gson.annotations.SerializedName

data class H2HResponse(
    @SerializedName("filters")
    val filters: Filters? = Filters(),
    @SerializedName("resultSet")
    val resultSet: ResultSet? = ResultSet(),
    @SerializedName("aggregates")
    val aggregates: Aggregates? = Aggregates(),
    @SerializedName("matches")
    val matches: List<Matche>? = listOf()
)