package com.example.dentflow_android.data.remote

// Dane do logowania
data class LoginRequest(
    val email: String,
    val password: String
)

// Odpowiedź po logowaniu
data class AuthResponse(
    val id: Long,
    val token: String,
    val userId: Long,
    val email: String,
    val tenantId: Long
)

// Dane do rejestracji
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String

)
