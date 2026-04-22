package com.example.soccerworld.data.remote.espn

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EspnClient {
    val api: EspnApi = Retrofit.Builder()
        .baseUrl("https://site.api.espn.com/apis/site/v2/sports/soccer/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(EspnApi::class.java)
}
