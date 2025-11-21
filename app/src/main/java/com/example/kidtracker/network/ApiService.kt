package com.example.kidtracker.network

import com.example.kidtracker.models.RegisterRequest
import com.example.kidtracker.models.RegisterResponse
import com.example.kidtracker.models.LoginRequest
import com.example.kidtracker.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/users/register")
    suspend fun registerUser(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/users/login")
    suspend fun loginUser(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}
