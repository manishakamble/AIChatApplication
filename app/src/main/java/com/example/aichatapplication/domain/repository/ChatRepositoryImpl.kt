package com.example.aichatapplication.domain.repository

import com.example.aichatapplication.BuildConfig
import com.example.aichatapplication.data.model.ChatMessage
import com.example.aichatapplication.data.model.Content
import com.example.aichatapplication.data.model.GeminiRequest
import com.example.aichatapplication.data.model.GenerationConfig
import com.example.aichatapplication.data.model.Part
import com.example.aichatapplication.data.remote.GeminiApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.plus

@Singleton
class ChatRepositoryImpl @Inject constructor(private val apiService: GeminiApiService) : ChatRepository {

    // System prompt — sets the AI's personality and constraints
    // This is sent with every request as a systemInstruction
    private val systemPrompt = """
        You are a helpful, friendly, and knowledgeable AI assistant.
        Keep responses concise but complete.
        Use markdown formatting when helpful (bullet points, code blocks, etc.)
        If you don't know something, say so honestly.
    """.trimIndent()

    override suspend fun sendMessage(userMessage: String, history: List<ChatMessage>): Flow<Result<ChatMessage>>
    = flow {

        try {
            // ── Step 1: Convert our ChatMessage history to Gemini's Content format
            // Gemini requires alternating user/model turns — we filter loading/error messages
            val conversationHistory = history
                .filter { !it.isLoading && !it.isError }   // Skip UI-only states
                .map { message ->
                    Content(
                        role = if (message.isUser) "user" else "model",
                        parts = listOf(Part(text = message.text))
                    )
                }

            // ── Step 2: Add the new user message
            val allContents = conversationHistory + Content(
                role = "user",
                parts = listOf(Part(text = userMessage))
            )

            // ── Step 3: Build the request
            val request = GeminiRequest(
                contents = allContents,
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 2048
                ),
                systemInstruction = Content(
                    role = "user",
                    parts = listOf(Part(text = systemPrompt))
                )
            )

            // ── Step 4: Make the API call (suspend function — doesn't block UI thread)
            val response = apiService.generateContent(
                url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent",
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )

            // ── Step 5: Handle API error (e.g., quota exceeded, invalid key)
            if (response.error != null) {
                emit(Result.failure(Exception("API Error ${response.error.code}: ${response.error.message}")))
                return@flow
            }

            // ── Step 6: Extract text from response
            val responseText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?: "Sorry, I couldn't generate a response. Please try again."

            // ── Step 7: Check if response was blocked by safety filters
            val finishReason = response.candidates?.firstOrNull()?.finishReason
            val safetyBlocked = finishReason == "SAFETY"

            val finalText = if (safetyBlocked) {
                "I can't respond to that request due to safety guidelines."
            } else {
                responseText
            }

            // ── Step 8: Emit success with the AI's response as a ChatMessage
            emit(
                Result.success(
                    ChatMessage(
                        text = finalText,
                        isUser = false
                    )
                )
            )

        } catch (e: Exception) {
            // Network error, timeout, JSON parsing failure, etc.
            emit(Result.failure(e))
        }
    }
}