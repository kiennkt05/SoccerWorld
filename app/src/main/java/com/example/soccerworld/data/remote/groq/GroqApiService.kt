package com.example.soccerworld.data.remote.groq

import com.example.soccerworld.data.model.GroqChatRequest
import com.example.soccerworld.data.model.GroqChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface GroqApiService {
    @POST("openai/v1/chat/completions")
    suspend fun createChatCompletion(
        @Body request: GroqChatRequest
    ): GroqChatResponse
}
