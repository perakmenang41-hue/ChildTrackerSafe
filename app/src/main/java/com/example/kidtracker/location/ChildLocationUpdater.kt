package com.example.kidtracker.location

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.example.kidtracker.system.DataStoreManager // <- use new one
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChildLocationUpdater(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val dataStoreManager: DataStoreManager
) {

    /**
     * Updates the child location and status in Firestore.
     * Fetches extra info (age, country, email, childName, registeredAt) from registered_users collection.
     */
    fun updateLocation(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            // Use Flow to get child UID from DataStore
            val uniqueId = dataStoreManager.childUID.firstOrNull() ?: return@launch

            try {
                // Query registered_users by uniqueId
                val querySnapshot = firestore.collection("registered_users")
                    .whereEqualTo("uniqueId", uniqueId)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    println("No registered user found with UID: $uniqueId")
                    return@launch
                }

                val userDoc = querySnapshot.documents[0]
                val age = userDoc.getString("age") ?: ""
                val country = userDoc.getString("country") ?: ""
                val email = userDoc.getString("email") ?: ""
                val childNameFromDB = userDoc.getString("name") ?: "Child"
                val registeredAt = userDoc.getTimestamp("registeredAt") ?: Timestamp.now()

                // Get battery percentage
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val batteryPct = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                } else 50

                val status = "online"

                // Prepare data
                val data = hashMapOf(
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "battery" to batteryPct,
                    "status" to status,
                    "childName" to childNameFromDB,
                    "age" to age,
                    "country" to country,
                    "email" to email,
                    "registeredAt" to registeredAt,
                    "lastUpdated" to Timestamp.now()
                )

                // Update child_locations collection
                firestore.collection("child_locations")
                    .document(uniqueId)
                    .set(data)
                    .addOnSuccessListener {
                        println("Location & info updated for UID: $uniqueId")
                    }
                    .addOnFailureListener { e ->
                        println("Failed to update location: ${e.message}")
                    }

            } catch (e: Exception) {
                println("Error fetching registered user data: ${e.message}")
            }
        }
    }
}
