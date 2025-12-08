// com/example/kidtracker/location/ChildLocationUpdater.kt
package com.example.kidtracker.location

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.kidtracker.system.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChildLocationUpdater(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val dataStoreManager: DataStoreManager
) {

    /**
     * Writes to collection "child_position" (this is the parent app's expected doc)
     * Document ID = uniqueId stored in DataStore
     */
    fun updateLocation(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            val uniqueId = dataStoreManager.getChildUID() ?: return@launch

            try {
                // battery percentage
                val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val batteryPct = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                } else 50

                val status = "online"

                val payload = hashMapOf(
                    "lat" to latitude,
                    "lon" to longitude,
                    "battery" to batteryPct,
                    "status" to status,
                    "lastUpdated" to Timestamp.now(),
                    "lastUpdatedRaw" to System.currentTimeMillis()
                )

                firestore.collection("child_position")
                    .document(uniqueId)
                    .set(payload, SetOptions.merge())
                    .addOnSuccessListener {
                        println("✅ child_position updated for UID: $uniqueId")
                    }
                    .addOnFailureListener { e ->
                        println("❌ Firestore update failed: ${e.message}")
                    }

            } catch (e: Exception) {
                println("❌ ChildLocationUpdater error: ${e.message}")
            }
        }
    }
}
