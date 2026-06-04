package com.example.soccerworld.data

import android.content.Context
import com.example.soccerworld.R
import com.example.soccerworld.data.agent.ToolExecutor
import com.example.soccerworld.data.agent.ToolRegistry
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
    private val toolExecutor: ToolExecutor,
    private val context: Context
) {
    val allMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    suspend fun sendMessage(
        userMessage: String,
        currentHistory: List<ChatMessageEntity>,
        onToolStep: suspend (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userEntity = ChatMessageEntity(
                role = "user",
                content = userMessage,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(userEntity)

            val systemPrompt = context.getString(R.string.chatbot_system_prompt)
            val modelName = context.getString(R.string.chatbot_model)

            val conversationMessages = mutableListOf<GroqMessage>()
            conversationMessages.add(GroqMessage(role = "system", content = systemPrompt))
            
            // Không gửi lịch sử hội thoại dài đi để tránh phồng token gây lỗi Rate Limit (429) do các câu hỏi demo độc lập
            val recentMessages = currentHistory.takeLast(0)
            recentMessages.forEach {
                conversationMessages.add(GroqMessage(role = it.role, content = it.content))
            }
            conversationMessages.add(GroqMessage(role = "user", content = userMessage))

            val maxRounds = 5
            for (round in 1..maxRounds) {
                if (round > 1) {
                    kotlinx.coroutines.delay(2500) // Delay to avoid Groq rate limit
                }

                val request = GroqChatRequest(
                    model = modelName,
                    messages = conversationMessages,
                    tools = ToolRegistry.allTools(),
                    tool_choice = if (round == maxRounds) "none" else "auto"
                )

                val response = try {
                    groqApi.createChatCompletion(request)
                } catch (http: retrofit2.HttpException) {
                    if (http.code() == 429) {
                        // Rate limited — wait 5s then retry once
                        kotlinx.coroutines.delay(5000L)
                        try {
                            groqApi.createChatCompletion(request)
                        } catch (retry: retrofit2.HttpException) {
                            if (retry.code() == 429) {
                                return@withContext Result.failure(
                                    Exception(context.getString(R.string.chatbot_error_rate_limit))
                                )
                            }
                            throw retry
                        }
                    } else throw http
                }

                val choice = response.choices.firstOrNull() ?: return@withContext Result.failure(Exception("Empty choice"))
                
                val responseMsg = choice.message
                if (!responseMsg.tool_calls.isNullOrEmpty()) {
                    conversationMessages.add(responseMsg)
                    
                    for (toolCall in responseMsg.tool_calls) {
                        onToolStep(getToolStepLabel(toolCall.function.name))
                        val toolResult = toolExecutor.execute(toolCall)
                        conversationMessages.add(
                            GroqMessage(
                                role = "tool",
                                content = toolResult,
                                tool_call_id = toolCall.id,
                                name = toolCall.function.name
                            )
                        )
                    }
                    continue
                }

                val rawReply = responseMsg.content ?: return@withContext Result.failure(Exception(context.getString(R.string.chatbot_error_api)))
                val assistantReply = sanitizeReply(rawReply)

                // Nếu sau khi lọc không còn nội dung hữu ích → bỏ qua, cho LLM thử vòng tiếp theo
                if (assistantReply.isBlank()) continue

                val assistantEntity = ChatMessageEntity(
                    role = "assistant",
                    content = assistantReply,
                    timestamp = System.currentTimeMillis()
                )
                chatDao.insertMessage(assistantEntity)

                return@withContext Result.success(assistantReply)
            }
            Result.failure(Exception("Exceeded maximum tool rounds"))
        } catch (http: retrofit2.HttpException) {
            if (http.code() == 429) {
                Result.failure(Exception(context.getString(R.string.chatbot_error_rate_limit)))
            } else {
                http.printStackTrace()
                Result.failure(Exception(context.getString(R.string.chatbot_error_network)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(context.getString(R.string.chatbot_error_network)))
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        chatDao.clearAllMessages()
    }

    private fun getToolStepLabel(toolName: String): String {
        return when (toolName) {
            "search_team_or_player" -> "🔍 Đang tìm kiếm..."
            "get_league_standings" -> "📊 Đang tra bảng xếp hạng..."
            "get_head_to_head" -> "⚔️ Đang lấy lịch sử đối đầu..."
            "get_team_matches" -> "📅 Đang xem phong độ gần đây..."
            "get_match_detail" -> "📋 Đang phân tích chi tiết trận đấu..."
            "get_fixtures" -> "📅 Đang tra lịch thi đấu..."
            "get_team_players" -> "👥 Đang xem đội hình..."
            "get_top_scorers" -> "🏆 Đang tra vua phá lưới..."
            "get_team_transfers" -> "🔀 Đang xem chuyển nhượng..."
            else -> "⚙️ Đang thực thi dữ liệu..."
        }
    }

    /**
     * Removes leaked tool-call markup that some smaller models incorrectly embed
     * directly into the content field instead of using structured tool_calls.
     * Examples cleaned up:
     *   /function= search_team_or_player({'query': 'X'})<function>
     *   <function>...</function>
     */
    private fun sanitizeReply(raw: String): String {
        return raw
            // Remove /function= ... <function> blocks (llama-3.1-8b style leakage)
            .replace(Regex("/function=\\s*[\\w_]+\\([^)]*\\)<function>"), "")
            // Remove any remaining <function> or </function> tags
            .replace(Regex("</?function>"), "")
            // Remove lines that are only whitespace
            .lines().filter { it.isNotBlank() }.joinToString("\n")
            .trim()
    }
}
