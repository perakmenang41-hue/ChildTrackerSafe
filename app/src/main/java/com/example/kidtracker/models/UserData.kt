package com.example.kidtracker.models

data class UserData(
    val childName: String,
    val email: String,
    val uniqueId: String,
    val registeredAt: String // or Timestamp if you want
)
