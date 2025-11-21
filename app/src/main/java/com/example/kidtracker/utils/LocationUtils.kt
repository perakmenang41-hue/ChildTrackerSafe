package com.example.kidtracker.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.kidtracker.location.ChildLocationService
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

@Composable
fun LocationPermissionAndGpsHandler(
    onLocationReady: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    // Request location permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            checkGpsEnabled(activity, onLocationReady)
        } else {
            Toast.makeText(context, "Location permissions are required", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

        permissionLauncher.launch(permissions)
    }
}

// GPS check function
private fun checkGpsEnabled(activity: Activity, onLocationReady: () -> Unit) {
    val locationRequest = LocationRequest.create().apply {
        interval = 5000
        fastestInterval = 2000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true)

    val client = LocationServices.getSettingsClient(activity)
    val task = client.checkLocationSettings(builder.build())

    task.addOnSuccessListener {
        onLocationReady() // GPS enabled
    }

    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            val intentSenderRequest =
                androidx.activity.result.IntentSenderRequest.Builder(exception.resolution).build()

            // Launch GPS enable intent
            (activity as? androidx.activity.ComponentActivity)?.let { act ->
                val launcher = act.activityResultRegistry.register(
                    "GPS_ENABLE",
                    ActivityResultContracts.StartIntentSenderForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        onLocationReady()
                    } else {
                        Toast.makeText(
                            activity,
                            "GPS is required for tracking",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                launcher.launch(intentSenderRequest)
            }
        }
    }
}

// Start location service
fun startLocationService(context: Context) {
    val intent = Intent(context, ChildLocationService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}
