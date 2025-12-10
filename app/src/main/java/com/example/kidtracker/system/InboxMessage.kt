package com.example.kidtracker.system

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class InboxMessage(
    val id: Long,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
) {
    companion object {
        fun toJson(list: List<InboxMessage>): String {
            return Json.encodeToString(list)
        }

        fun fromJson(json: String): List<InboxMessage> {
            return if (json.isBlank()) emptyList()
            else Json.decodeFromString(json)
        }
    }
}
