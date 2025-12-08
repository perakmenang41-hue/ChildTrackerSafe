// com/example/kidtracker/location/ChildLocationService.kt
package com.example.kidtracker.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import kotlinx.coroutines.cancel
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.kidtracker.R
import com.example.kidtracker.system.DataStoreManager
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale

class ChildLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var dataStoreManager: DataStoreManager

    private lateinit var movementAnalyzer: ChildMovementAnalyzer
    private lateinit var locationUpdater: ChildLocationUpdater

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val CHANNEL_ID = "KidTrackerChannel"

    override fun onCreate() {
        super.onCreate()
        dataStoreManager = DataStoreManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        movementAnalyzer = ChildMovementAnalyzer(this, firestore, dataStoreManager)
        locationUpdater = ChildLocationUpdater(this, firestore, dataStoreManager)

        createNotificationChannel()
        startForeground(1, buildNotification("Tracking location in background"))

        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5_000L
        ).setMinUpdateIntervalMillis(5_000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    try {
                        // update Firestore via updater
                        locationUpdater.updateLocation(location.latitude, location.longitude)

                        // feed movement analyzer
                        movementAnalyzer.onNewLocation(location)

                        // GEO address done on background thread if needed (logging)
                        serviceScope.launch {
                            try {
                                val address = try {
                                    val geocoder = Geocoder(applicationContext, Locale.getDefault())
                                    val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                    }
                                    list?.firstOrNull()?.getAddressLine(0) ?: "Unknown"
                                } catch (e: Exception) {
                                    "Unknown"
                                }
                                updateNotification(location, address)
                            } catch (e: Exception) {
                                Log.e("ChildLocationService", "Error in locationResult: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ChildLocationService", "Error processing location: ${e.message}")
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ChildLocationService", "Location permission not granted")
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun updateNotification(location: Location, address: String) {
        val updatedNotif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Child Location Tracking")
            .setContentText("📍 %.5f, %.5f (±%.0fm)".format(location.latitude, location.longitude, location.accuracy))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(1, updatedNotif)
        } catch (e: SecurityException) {
            Log.e("ChildLocationService", "Notification failed: ${e.message}")
        }
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Kid Tracker")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kid Tracker Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.w("ChildLocationService", "removeLocationUpdates failed: ${e.message}")
        }
        serviceScope.cancel()
        movementAnalyzer.stop()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
