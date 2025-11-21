package com.example.kidtracker.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("uniqueId")
    val uniqueId: String
)
