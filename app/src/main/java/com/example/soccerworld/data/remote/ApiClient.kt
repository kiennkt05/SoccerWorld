package com.example.soccerworld.data.remote

import com.example.soccerworld.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://flashlive-sports.p.rapidapi.com"
    private const val RAPID_API_HOST = "flashlive-sports.p.rapidapi.com"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request: Request = chain.request().newBuilder()
                    .addHeader("x-rapidapi-key", BuildConfig.FLASHLIVE_API_KEY)
                    .addHeader("x-rapidapi-host", RAPID_API_HOST)
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}