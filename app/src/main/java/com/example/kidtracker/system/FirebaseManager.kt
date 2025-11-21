package com.example.kidtracker.system

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

object FirebaseManager {

    private val functions: FirebaseFunctions = Firebase.functions

    fun sendUidEmail(uid: String, email: String, name: String) {
        val data: HashMap<String, Any> = hashMapOf(
            "uid" to uid,
            "email" to email,
            "name" to name
        )

        functions
            .getHttpsCallable("sendChildUidEmail")
            .call(data)
            .addOnSuccessListener {
                Log.d("FirebaseManager", "Email sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseManager", "Email send failed: ${e.message}")
            }
    }
}
