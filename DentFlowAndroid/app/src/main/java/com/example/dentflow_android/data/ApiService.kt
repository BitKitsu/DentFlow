package com.example.dentflow_android.data

import retrofit2.http.GET

interface ApiService {
    @GET("users") // To końcówka adresu URL
    suspend fun getUsers(): List<UserResponse>
}