package com.example.soccerworld.data.remote.sportsdb

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SportsDbClient {
    val api: SportsDbApi = Retrofit.Builder()
        .baseUrl("https://www.thesportsdb.com/api/v1/json/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SportsDbApi::class.java)
}
