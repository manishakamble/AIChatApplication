// =============================================
// FILE: AIChatApplication.kt
// =============================================
// Required by Hilt — must annotate Application class with @HiltAndroidApp.
// This triggers Hilt's code generation and sets up the DI container.

package com.example.aichatapplication

// =============================================
// FILE: MainActivity.kt
// =============================================
// Single Activity — Compose apps typically use just one Activity.
// Navigation between screens is handled by NavHost (Compose Navigation),
// not by starting new Activities.

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge    // Full screen content behind status bar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.aichatapplication.presentation.ChatScreen
import com.example.aichatapplication.ui.theme.AIChatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint  // Required for Hilt to inject into this Activity
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Content goes under status bar & nav bar

        setContent {
            AIChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen()
                }
            }
        }
    }
}




