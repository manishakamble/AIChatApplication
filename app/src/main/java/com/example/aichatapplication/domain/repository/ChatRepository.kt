package com.example.aichatapplication.domain.repository

import com.example.aichatapplication.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    /**
     * Send a message and get the AI response.
     *
     * Returns a Flow<Result<ChatMessage>> so the ViewModel can:
     * - Observe loading state (collect emissions over time)
     * - Handle success (AI response)
     * - Handle errors gracefully
     *
     * @param userMessage  The text the user typed
     * @param history      Previous messages for context (Gemini is stateless,
     *                     so we must send full conversation history each time)
     */
    suspend fun sendMessage(userMessage: String, history: List<ChatMessage>): Flow<Result<ChatMessage>>
}