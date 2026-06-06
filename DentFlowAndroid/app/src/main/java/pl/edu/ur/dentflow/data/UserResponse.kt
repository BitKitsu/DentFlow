package pl.edu.ur.dentflow.data

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String
)
