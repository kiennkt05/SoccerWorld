package com.example.soccerworld.data.remote.groq

import android.util.Log
import com.example.soccerworld.BuildConfig
import com.example.soccerworld.data.model.GroqMessage
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

class GroqMessageSerializer : JsonSerializer<GroqMessage> {
    override fun serialize(
        src: GroqMessage,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val obj = JsonObject()
        obj.addProperty("role", src.role)
        
        when (src.role) {
            "system", "user" -> {
                // Only role and content are allowed/needed for system/user messages
                obj.addProperty("content", src.content)
            }
            "assistant" -> {
                if (!src.tool_calls.isNullOrEmpty()) {
                    // Groq strictly requires "content": null explicitly when tool_calls are present
                    obj.add("content", JsonNull.INSTANCE)
                    obj.add("tool_calls", context.serialize(src.tool_calls))
                } else {
                    obj.addProperty("content", src.content)
                }
            }
            "tool" -> {
                // Tool response messages must have role, content, tool_call_id, and name
                obj.addProperty("content", src.content)
                obj.addProperty("tool_call_id", src.tool_call_id)
                obj.addProperty("name", src.name)
            }
            else -> {
                // Fallback for safety
                if (src.content != null) {
                    obj.addProperty("content", src.content)
                }
                if (!src.tool_calls.isNullOrEmpty()) {
                    obj.add("tool_calls", context.serialize(src.tool_calls))
                }
                if (src.tool_call_id != null) {
                    obj.addProperty("tool_call_id", src.tool_call_id)
                }
                if (src.name != null) {
                    obj.addProperty("name", src.name)
                }
            }
        }
        return obj
    }
}

object GroqApiClient {
    private const val BASE_URL = "https://api.groq.com/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                // Add Authorization and Content-Type headers
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor { chain ->
                // Lightweight payload logger for debugging
                val request = chain.request()
                val requestBody = request.body
                var requestBodyString = ""
                if (requestBody != null) {
                    val buffer = Buffer()
                    requestBody.writeTo(buffer)
                    requestBodyString = buffer.readUtf8()
                }
                Log.d("GroqApi", "--> Sending Request: ${request.method} ${request.url}\nHeaders: ${request.headers}\nBody: $requestBodyString")
                
                val response = try {
                    chain.proceed(request)
                } catch (e: Exception) {
                    Log.e("GroqApi", "<-- Request Failed: ${e.message}", e)
                    throw e
                }
                
                val responseBody = response.body
                var responseBodyString = ""
                if (responseBody != null) {
                    responseBodyString = responseBody.string()
                }
                Log.d("GroqApi", "<-- Received Response: ${response.code}\nHeaders: ${response.headers}\nBody: $responseBodyString")
                
                val newResponseBody = responseBodyString.toResponseBody(responseBody?.contentType())
                response.newBuilder().body(newResponseBody).build()
            }
            .build()
    }

    val api: GroqApiService by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(GroqMessage::class.java, GroqMessageSerializer())
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GroqApiService::class.java)
    }
}
