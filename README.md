# AI Chat App — Native Kotlin + Gemini AI

An AI-powered chat app built with Kotlin, Jetpack Compose, and Gemini AI.

## Tech Stack

- **Kotlin** + **Jetpack Compose** (UI)
- **Gemini API** (Google AI)
- **MVVM + Clean Architecture**
- **Hilt** (Dependency Injection)
- **Coroutines + Flow** (Async)
- **Retrofit** (Networking)

## Project Structure
```
app/
├── data/
│   ├── model/          # Data classes
│   ├── remote/         # Retrofit API
├── domain/
│   ├── model/          # Domain models
│   └── repository/     # Repository interface
├── presentation/
│   ├── chat/           # Chat screen UI + ViewModel
│   └── theme/          # App theme
├── di/                 # Hilt modules
└── MainActivity.kt
