package com.example.soccerworld.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soccerworld.data.ChatRepository
import com.example.soccerworld.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    val messages: StateFlow<List<ChatMessageEntity>> = repository.allMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _toolSteps = MutableStateFlow<List<String>>(emptyList())
    val toolSteps = _toolSteps.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _toolSteps.value = emptyList()
            
            val result = repository.sendMessage(
                userMessage = content, 
                currentHistory = messages.value,
                onToolStep = { step ->
                    _toolSteps.value = _toolSteps.value + step
                }
            )
            
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
            _toolSteps.value = emptyList()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun dismissError() {
        _error.value = null
    }
}
