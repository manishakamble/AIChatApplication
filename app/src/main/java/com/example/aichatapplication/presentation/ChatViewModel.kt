
// The brain of the chat screen.
// Manages all UI state, handles user actions, calls the repository.
//
// Key Jetpack components used:
// - ViewModel: survives configuration changes (screen rotation)
// - StateFlow: observable, hot stream of UI state — Compose collects it
// - viewModelScope: auto-cancelled coroutine scope tied to ViewModel lifecycle

package com.example.aichatapplication.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aichatapplication.data.model.ChatMessage
import com.example.aichatapplication.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ────────────────────────────────────────────────────

/**
 * Single source of truth for the Chat screen UI.
 * Compose observes this and re-renders only what changed.
 *
 * This is the "Unidirectional Data Flow" (UDF) pattern:
 *   User Action → ViewModel → State Update → UI Re-render
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

// ── ViewModel ───────────────────────────────────────────────────

@HiltViewModel  // Hilt injects dependencies (ChatRepository) automatically
class ChatViewModel @Inject constructor(private val chatRepository: ChatRepository) : ViewModel() {

    // Private mutable state — only ViewModel can modify it
    private val _uiState = MutableStateFlow(ChatUiState())

    // Public read-only state — UI observes this
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // ── Welcome message shown when app starts
    init {
        _uiState.update { state ->
            state.copy(
                messages = listOf(
                    ChatMessage(
                        text = "👋 Hi! I'm your AI assistant powered by Gemini. How can I help you today?",
                        isUser = false
                    )
                )
            )
        }
    }

    // ── Called every time user types in the input field
    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text, error = null) }
    }

    // ── Called when user taps Send button
    fun sendMessage() {
        val messageText = _uiState.value.inputText.trim()

        // Guard: don't send empty messages or while already loading
        if (messageText.isBlank() || _uiState.value.isLoading) return

        viewModelScope.launch {
            // ── 1. Add user's message to the list immediately (optimistic UI)
            val userMessage = ChatMessage(text = messageText, isUser = true)

            // ── 2. Add a "typing..." placeholder for the AI
            val loadingMessage = ChatMessage(
                text = "...",
                isUser = false,
                isLoading = true
            )

            _uiState.update { state ->
                state.copy(
                    messages = state.messages + userMessage + loadingMessage,
                    inputText = "",     // Clear input field
                    isLoading = true,
                    error = null
                )
            }

            // ── 3. Call repository — pass history WITHOUT the loading placeholder
            val historyForApi = _uiState.value.messages
                .filter { !it.isLoading }   // Exclude the placeholder we just added

            chatRepository.sendMessage(
                userMessage = messageText,
                history = historyForApi.dropLast(1) // Don't include the just-added user message
                                                     // (sendMessage adds it internally)
            ).collect { result ->

                result.fold(
                    onSuccess = { aiMessage ->
                        // ── 4. Replace loading placeholder with real AI response
                        _uiState.update { state ->
                            state.copy(
                                messages = state.messages
                                    .filterNot { it.isLoading }  // Remove placeholder
                                    + aiMessage,                  // Add real response
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { error ->
                        // ── 5. Show error in place of loading placeholder
                        val errorMessage = ChatMessage(
                            text = "⚠️ ${getReadableError(error.message)}",
                            isUser = false,
                            isError = true
                        )
                        _uiState.update { state ->
                            state.copy(
                                messages = state.messages
                                    .filterNot { it.isLoading }
                                    + errorMessage,
                                isLoading = false,
                                error = error.message
                            )
                        }
                    }
                )
            }
        }
    }

    // ── Clear all chat history
    fun clearChat() {
        _uiState.update { ChatUiState() }
        // Re-trigger init welcome message
        _uiState.update { state ->
            state.copy(
                messages = listOf(
                    ChatMessage(
                        text = "👋 Hi! I'm your AI assistant powered by Gemini. How can I help you today?",
                        isUser = false
                    )
                )
            )
        }
    }

    // ── Retry the last failed message
    fun retryLastMessage() {
        val lastUserMessage = _uiState.value.messages
            .lastOrNull { it.isUser }
            ?.text ?: return

        // Remove the error message and restore the input
        _uiState.update { state ->
            state.copy(
                messages = state.messages.filterNot { it.isError },
                inputText = lastUserMessage
            )
        }
    }

    /**
     * Convert technical errors to user-friendly messages.
     * Senior tip: never show raw stack traces or API error codes to users.
     */
    private fun getReadableError(message: String?): String {
        return when {
            message == null -> "Something went wrong. Please try again."
            message.contains("timeout", ignoreCase = true) ->
                "Request timed out. Check your internet connection."
            message.contains("401") ->
                "Invalid API key. Please check your configuration."
            message.contains("429") ->
                "Too many requests. Please wait a moment and try again."
            message.contains("quota", ignoreCase = true) ->
                "API quota exceeded. Please try again later."
            message.contains("Unable to resolve host") ->
                "No internet connection. Please check your network."
            else -> "Something went wrong. Please try again."
        }
    }
}
