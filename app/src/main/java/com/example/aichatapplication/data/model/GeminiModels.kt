
// These data classes map exactly to Gemini API's JSON structure.
// Retrofit uses Gson to auto-convert JSON ↔ Kotlin objects.

package com.example.aichatapplication.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

// ── REQUEST MODELS ──────────────────────────────────────────────

/**
 * Top-level request body sent to Gemini API.
 *
 * POST /v1beta/models/gemini-pro:generateContent
 * {
 *   "contents": [...],
 *   "generationConfig": {...},
 *   "safetySettings": [...]
 * }
 */
data class GeminiRequest(
    val contents: List<Content>,

    @SerializedName("generationConfig")
    val generationConfig: GenerationConfig = GenerationConfig(),

    @SerializedName("systemInstruction")
    val systemInstruction: Content? = null
)

/**
 * Represents one message in the conversation.
 * role = "user" or "model" (Gemini's term for assistant)
 */
data class Content(
    val role: String,           // "user" or "model"
    val parts: List<Part>
)

/**
 * The actual text content inside a message.
 * Gemini supports multi-modal parts (text, image, etc.)
 * Here we only use text.
 */
data class Part(
    val text: String
)

/**
 * Controls how the model generates output.
 */
data class GenerationConfig(
    val temperature: Float = 0.7f,      // 0 = deterministic, 1 = creative
    val topK: Int = 40,                 // Consider top-40 tokens at each step
    val topP: Float = 0.95f,            // Nucleus sampling threshold
    val maxOutputTokens: Int = 2048     // Max response length (~1500 words)
)

// ── RESPONSE MODELS ─────────────────────────────────────────────

/**
 * Top-level response from Gemini API.
 */
data class GeminiResponse(
    val candidates: List<Candidate>?,
    val error: GeminiError?             // Present if API returns an error
)

data class Candidate(
    val content: Content?,
    val finishReason: String?           // "STOP", "MAX_TOKENS", "SAFETY", etc.
)

data class GeminiError(
    val code: Int,
    val message: String,
    val status: String
)

// ── DOMAIN MODEL ────────────────────────────────────────────────

/**
 * ChatMessage is our internal model used in the UI layer.
 * Decoupled from the API model — clean architecture principle.
 *
 * @param id        Unique ID (timestamp-based) for LazyColumn keys
 * @param text      The message content
 * @param isUser    true = user bubble, false = AI bubble
 * @param isLoading true = show typing indicator (AI is thinking)
 * @param isError   true = show error state
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false,
    val isError: Boolean = false
)
