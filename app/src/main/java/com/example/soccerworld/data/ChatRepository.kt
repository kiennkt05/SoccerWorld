package com.example.soccerworld.data

import android.content.Context
import com.example.soccerworld.R
import com.example.soccerworld.data.local.ChatDao
import com.example.soccerworld.data.local.entity.ChatMessageEntity
import com.example.soccerworld.data.model.GroqChatRequest
import com.example.soccerworld.data.model.GroqMessage
import com.example.soccerworld.data.remote.groq.GroqApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChatRepository(
    private val chatDao: ChatDao,
    private val groqApi: GroqApiService,
    private val context: Context
) {
    val allMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    suspend fun sendMessage(userMessage: String, currentHistory: List<ChatMessageEntity>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userEntity = ChatMessageEntity(
                role = "user",
                content = userMessage,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(userEntity)

            val systemPrompt = context.getString(R.string.chatbot_system_prompt)
            val modelName = context.getString(R.string.chatbot_model)

            val apiMessages = mutableListOf<GroqMessage>()
            apiMessages.add(GroqMessage(role = "system", content = systemPrompt))
            
            val recentMessages = currentHistory.takeLast(20)
            recentMessages.forEach {
                apiMessages.add(GroqMessage(role = it.role, content = it.content))
            }
            apiMessages.add(GroqMessage(role = "user", content = userMessage))

            val request = GroqChatRequest(
                model = modelName,
                messages = apiMessages
            )

            val response = groqApi.createChatCompletion(request)
            val assistantReply = response.choices.firstOrNull()?.message?.content ?: return@withContext Result.failure(Exception(context.getString(R.string.chatbot_error_api)))

            val assistantEntity = ChatMessageEntity(
                role = "assistant",
                content = assistantReply,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(assistantEntity)

            Result.success(assistantReply)
        } catch (e: Exception) {
            Result.failure(Exception(context.getString(R.string.chatbot_error_network)))
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        chatDao.clearAllMessages()
    }
}
