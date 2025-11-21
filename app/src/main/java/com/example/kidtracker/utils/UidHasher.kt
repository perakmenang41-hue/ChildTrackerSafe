package com.example.kidtracker.utils

import java.security.MessageDigest

object UidHasher {

    fun hashUID(uid: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(uid.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
