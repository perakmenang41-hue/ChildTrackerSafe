// com/example/kidtracker/location/ChildMovementAnalyzer.kt
package com.example.kidtracker.location

import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.kidtracker.R
import com.example.kidtracker.system.DataStoreManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.atan2

class ChildMovementAnalyzer(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val dataStoreManager: DataStoreManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val history = LinkedList<Point>()
    private val HISTORY_MAX = 6

    private val SPEED_THRESHOLD_MPS = 3.0f
    private val ANGLE_CHANGE_THRESHOLD_DEG = 45.0
    private val DIST_FROM_CENTER_THRESHOLD_M = 50
    private val MIN_POINTS_FOR_ANALYSIS = 3

    private val TAG = "ChildMovementAnalyzer"

    private data class Point(val lat: Double, val lon: Double, val timestampMs: Long, val accuracy: Float)

    fun onNewLocation(location: Location) {
        val p = Point(location.latitude, location.longitude, System.currentTimeMillis(), location.accuracy)
        synchronized(history) {
            history.add(0, p)   // FIXED
            while (history.size > HISTORY_MAX) history.removeLast()
        }
        scope.launch { analyze() }
    }

    private suspend fun analyze() {
        val snapshot: List<Point>
        synchronized(history) { snapshot = history.toList() }

        if (snapshot.size < MIN_POINTS_FOR_ANALYSIS) return

        val newest = snapshot[0]
        val prev = snapshot.getOrNull(1) ?: return

        val distArr = FloatArray(1)
        android.location.Location.distanceBetween(prev.lat, prev.lon, newest.lat, newest.lon, distArr)
        val distanceMeters = distArr[0]
        val timeDiffSec = maxOf(1.0, (newest.timestampMs - prev.timestampMs) / 1000.0)
        val speedMps = distanceMeters / timeDiffSec

        var angleChange = 0.0
        val older = snapshot.getOrNull(2)
        if (older != null) {
            val ang1 = atan2(prev.lon - older.lon, prev.lat - older.lat) * (180.0 / Math.PI)
            val ang2 = atan2(newest.lon - prev.lon, newest.lat - prev.lat) * (180.0 / Math.PI)
            angleChange = abs(ang2 - ang1)
            if (angleChange > 180) angleChange = 360 - angleChange
        }

        val safeCenter = snapshot.last()
        val distFromCenterArr = FloatArray(1)
        android.location.Location.distanceBetween(safeCenter.lat, safeCenter.lon, newest.lat, newest.lon, distFromCenterArr)
        val distFromCenter = distFromCenterArr[0]

        Log.d(TAG, "speed=$speedMps m/s angleChange=$angleChange distFromCenter=$distFromCenter")

        var score = 0
        if (speedMps > SPEED_THRESHOLD_MPS) score++
        if (angleChange > ANGLE_CHANGE_THRESHOLD_DEG) score++
        if (distFromCenter > DIST_FROM_CENTER_THRESHOLD_M) score++

        if (score >= 2) {
            handleAIDetection("wandering", speedMps, angleChange, distFromCenter)
        }
    }

    private fun handleAIDetection(type: String, speed: Double, angle: Double, distFromCenter: Float) {
        scope.launch {
            try {
                val uniqueId = dataStoreManager.getChildUID()
                if (uniqueId.isNullOrBlank()) {
                    Log.w(TAG, "No child UID available for AI alert.")
                } else {
                    val alertData = hashMapOf(
                        "aiAlert" to type,
                        "aiAlertAt" to Timestamp.now(),
                        "aiDetails" to hashMapOf(
                            "speed_mps" to speed,
                            "angle_deg" to angle,
                            "dist_from_center_m" to distFromCenter
                        )
                    )

                    firestore.collection("child_locations")
                        .document(uniqueId)
                        .set(alertData, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "AI alert written to Firestore for $uniqueId")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed to write AI alert: ${e.message}")
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error while sending AI alert: ${e.message}")
            }

            showLocalNotification("AI Alert: Child may be wandering!", "Speed: %.1fm/s • Dist: %.1fm".format(speed, distFromCenter))
        }
    }

    private fun showLocalNotification(title: String, content: String) {
        try {
            val channelId = "ai_alerts"
            // create channel previously in app if needed

            val notif = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            try {
                NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notif)
            } catch (se: SecurityException) {
                Log.e(TAG, "Notification permission rejected: ${se.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Notification failed: ${e.message}")
        }
    }

    fun stop() {
        scope.cancel()
    }
}
