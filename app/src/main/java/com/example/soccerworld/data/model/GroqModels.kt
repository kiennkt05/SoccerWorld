package com.example.soccerworld.data.model

data class GroqChatRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val tools: List<GroqToolDefinition>? = null,
    val tool_choice: String? = "auto",
    val temperature: Double = 0.7,
    val max_completion_tokens: Int = 4096,
    val stream: Boolean = false
)

data class GroqMessage(
    val role: String,
    val content: String? = null,
    val tool_calls: List<GroqToolCall>? = null,
    val tool_call_id: String? = null,
    val name: String? = null
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

data class GroqToolDefinition(
    val type: String = "function",
    val function: GroqFunctionDef
)

data class GroqFunctionDef(
    val name: String,
    val description: String,
    val parameters: GroqParameterSchema
)

data class GroqParameterSchema(
    val type: String = "object",
    val properties: Map<String, GroqPropertySchema>,
    val required: List<String> = emptyList()
)

data class GroqPropertySchema(
    val type: String,
    val description: String,
    val enum: List<String>? = null
)

data class GroqToolCall(
    val id: String,
    val type: String = "function",
    val function: GroqFunctionCall
)

data class GroqFunctionCall(
    val name: String,
    val arguments: String
)
