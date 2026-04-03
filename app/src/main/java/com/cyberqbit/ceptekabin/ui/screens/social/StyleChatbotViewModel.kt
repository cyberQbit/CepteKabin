package com.cyberqbit.ceptekabin.ui.screens.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StyleChatbotUiState(
    val isLoading: Boolean = false,
    val messages: List<ChatMessage> = emptyList()
)

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class StyleChatbotViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(StyleChatbotUiState())
    val uiState: StateFlow<StyleChatbotUiState> = _uiState.asStateFlow()

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            val newMessage = ChatMessage(userMessage, isFromUser = true)
            _uiState.update { it.copy(messages = it.messages + newMessage, isLoading = true) }
            try {
                // TODO: AI endpoint'e sor
                val response = ChatMessage("Stil önerisi yükleniyor...", isFromUser = false)
                _uiState.update { it.copy(messages = it.messages + response, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
