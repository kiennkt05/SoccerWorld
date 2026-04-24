package com.example.soccerworld.data.remote

import android.util.Log
import com.example.soccerworld.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.atomic.AtomicInteger

object ApiClient {
    private const val BASE_URL = "https://flashlive-sports.p.rapidapi.com"
    private const val RAPID_API_HOST = "flashlive-sports.p.rapidapi.com"
    private const val API_COUNTER_TAG = "ApiRequestCounter"
    private val requestCounter = AtomicInteger(0)

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val count = requestCounter.incrementAndGet()
                val request: Request = chain.request().newBuilder()
                    .addHeader("x-rapidapi-key", BuildConfig.FLASHLIVE_API_KEY)
                    .addHeader("x-rapidapi-host", RAPID_API_HOST)
                    .build()
                Log.d(
                    API_COUNTER_TAG,
                    "#$count -> ${request.method} ${request.url.encodedPath}?${request.url.encodedQuery.orEmpty()}"
                )
                val response = chain.proceed(request)
                Log.d(API_COUNTER_TAG, "#$count <- HTTP ${response.code}")
                response
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