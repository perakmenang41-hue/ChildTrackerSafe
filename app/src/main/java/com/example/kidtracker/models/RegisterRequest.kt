package com.example.kidtracker.models

data class RegisterRequest(
    val name: String,
    val email: String,
    val age: String,
    val country: String,
    val password: String
)
