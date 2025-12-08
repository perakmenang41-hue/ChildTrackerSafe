package com.example.kidtracker.models

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("uniqueId") val uniqueId: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("token") val token: String? = null // optional if backend returns token
)
