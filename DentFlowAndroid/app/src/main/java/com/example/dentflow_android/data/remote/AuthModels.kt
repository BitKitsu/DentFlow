package com.example.dentflow_android.data.remote

// Login credentials
data class LoginRequest(
    val email: String,
    val password: String
)

// Response from login / register / profile update
data class AuthResponse(
    val id: Long = 0L,
    val token: String,
    val userId: Long,
    val email: String,
    val tenantId: Long,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val addressStreet: String? = null,
    val addressCity: String? = null,
    val addressZip: String? = null,
    val addressCountry: String? = null
)

// Registration data
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String
)

// Profile update — all fields optional
data class UpdateProfileRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val addressStreet: String? = null,
    val addressCity: String? = null,
    val addressZip: String? = null,
    val addressCountry: String? = null
)
