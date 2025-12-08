package com.example.kidtracker.child

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.kidtracker.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.sqrt

class MotionSensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val firestore = FirebaseFirestore.getInstance()
    private var childUID: String = ""

    // Motion detection thresholds
    private val jumpThreshold = 18.0f
    private val runThreshold = 12.0f
    private val shakeThreshold = 8.0f

    // For shake detection
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var lastUpdateTime = 0L

    // -----------------------------
    // Dangerous zones
    // -----------------------------
    private val dangerousZones = listOf(
        Location("").apply { latitude = 3.1415; longitude = 101.6875 }, // Escalator A
        Location("").apply { latitude = 3.1420; longitude = 101.6880 }  // Exit B
    )
    private val dangerDistanceThreshold = 5f // meters

    // Track current location
    private var currentLocation: Location? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Get dynamic childUID from intent
        childUID = intent?.getStringExtra("childUID") ?: ""
        if (childUID.isBlank()) {
            Log.e("MotionSensorService", "childUID is empty! Service may not work.")
        }
        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val channelId = "motion_service_channel"
        val channelName = "Child Motion Detection"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("Child Activity Monitoring")
                .setContentText("Detecting movement for safety alerts")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Child Activity Monitoring")
                .setContentText("Detecting movement for safety alerts")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()
        }

        startForeground(1, notification)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = sqrt(x * x + y * y + z * z)

        // -----------------------------
        // Motion detection
        // -----------------------------
        if (magnitude > jumpThreshold) sendMotionAlert("Child is jumping")
        else if (magnitude > runThreshold) sendMotionAlert("Child is running")
        else if (magnitude > shakeThreshold) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime > 500) { // 500ms interval
                val deltaX = kotlin.math.abs(x - lastX)
                val deltaY = kotlin.math.abs(y - lastY)
                val deltaZ = kotlin.math.abs(z - lastZ)
                if (deltaX + deltaY + deltaZ > 12) sendMotionAlert("Child is shaking")
                lastX = x
                lastY = y
                lastZ = z
                lastUpdateTime = currentTime
            }
        }

        // -----------------------------
        // Dangerous zones check removed from here
        // -----------------------------
        // Now checked only on GPS location updates
    }

    // -----------------------------
    // Motion alert
    // -----------------------------
    private fun sendMotionAlert(message: String) {
        if (childUID.isBlank()) return
        try {
            firestore.collection("child_locations")
                .document(childUID)
                .update("aiAlert", message)
                .addOnSuccessListener { Log.d("MotionSensorService", "Alert sent: $message") }
                .addOnFailureListener { Log.e("MotionSensorService", "Failed to send alert", it) }
        } catch (e: Exception) {
            Log.e("MotionSensorService", "Firestore error: ${e.message}")
        }
    }

    // -----------------------------
    // Dangerous zone alert
    // -----------------------------
    private fun sendDangerAlert(message: String) {
        if (childUID.isBlank()) return
        try {
            firestore.collection("child_locations")
                .document(childUID)
                .update("storeAlert", message)
                .addOnSuccessListener { Log.d("MotionSensorService", "Danger alert sent: $message") }
                .addOnFailureListener { Log.e("MotionSensorService", "Failed danger alert", it) }
        } catch (e: Exception) {
            Log.e("MotionSensorService", "Firestore error: ${e.message}")
        }
    }

    // -----------------------------
    // Check dangerous zones using latest location
    // -----------------------------
    private fun checkDangerousZones(childLoc: Location) {
        dangerousZones.forEachIndexed { index, zone ->
            val distance = childLoc.distanceTo(zone)
            if (distance <= dangerDistanceThreshold) {
                sendDangerAlert("Child is near dangerous zone #${index + 1}")
            }
        }
    }

    // -----------------------------
    // Update child location
    // -----------------------------
    fun updateChildLocation(lat: Double, lon: Double) {
        currentLocation = Location("").apply {
            latitude = lat
            longitude = lon
        }

        // Check dangerous zones only when location changes
        currentLocation?.let {
            checkDangerousZones(it)
        }

        // Update Firestore with latest location
        if (childUID.isNotBlank()) {
            firestore.collection("child_locations")
                .document(childUID)
                .update(
                    mapOf(
                        "latitude" to lat,
                        "longitude" to lon
                    )
                )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
