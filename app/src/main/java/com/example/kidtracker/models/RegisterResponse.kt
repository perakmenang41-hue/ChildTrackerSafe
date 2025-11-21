package com.example.kidtracker.models

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("uniqueId")
    val uniqueId: String? = null
)
