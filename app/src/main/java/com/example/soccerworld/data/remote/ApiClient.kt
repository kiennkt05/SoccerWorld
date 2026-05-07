package com.example.soccerworld.data.remote

import android.util.Log
import com.example.soccerworld.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.soccerworld.data.remote.flashlive.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
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
            .addConverterFactory(GsonConverterFactory.create(
                GsonBuilder()
                    .registerTypeAdapter(SearchItemDto::class.java, SearchItemDeserializer())
                    .create()
            ))
            .build()
            .create(ApiService::class.java)
    }
}

class SearchItemDeserializer : JsonDeserializer<SearchItemDto> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): SearchItemDto {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("TYPE")?.asString ?: "unknown"
        return when (type) {
            "team", "participants" -> context.deserialize(jsonObject, TeamSearchItemDto::class.java)
            "playersInTeam" -> context.deserialize(jsonObject, PlayerSearchItemDto::class.java)
            "tournament", "tournament_templates" -> context.deserialize(jsonObject, TournamentSearchItemDto::class.java)
            else -> context.deserialize(jsonObject, UnknownSearchItemDto::class.java)
        }
    }
}