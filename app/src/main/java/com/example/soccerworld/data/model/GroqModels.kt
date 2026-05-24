package com.example.soccerworld.data.model

data class GroqChatRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.7,
    val max_completion_tokens: Int = 1024,
    val stream: Boolean = false
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqChatResponse(
    val id: String,
    val choices: List<GroqChoice>,
    val usage: GroqUsage?
)

data class GroqChoice(
    val index: Int,
    val message: GroqMessage,
    val finish_reason: String?
)

data class GroqUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)
