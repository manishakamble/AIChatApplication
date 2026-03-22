package com.example.aichatapplication.data.remote

import com.example.aichatapplication.data.model.GeminiRequest
import com.example.aichatapplication.data.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface GeminiApiService {

    /**
     * Calls Gemini's generateContent endpoint.
     *
     * Full URL = BASE_URL + endpoint + ?key=API_KEY
     * BASE_URL  = "https://generativelanguage.googleapis.com/v1beta/models/"
     * endpoint  = "gemini-1.5-flash:generateContent"
     *
     * We use gemini-1.5-flash because:
     * - Faster than gemini-pro (better for chat UX)
     * - Free tier available
     * - Strong at conversation tasks
     *
     * @param apiKey  Your Gemini API key (from BuildConfig)
     * @param request The request body with conversation history
     */
    @POST
    suspend fun generateContent(
        @Url url: String,           // ← full URL passed at call site
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}