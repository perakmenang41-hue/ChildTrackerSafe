package com.example.kidtracker.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

@Composable
fun CheckAndEnableLocation(
    context: Context,
    onLocationReady: () -> Unit
) {
    val activity = context as Activity

    // Launcher for location permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Location permission is required", Toast.LENGTH_LONG).show()
            }
        }
    )

    // Launcher for GPS resolution
    val gpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(context, "Location Enabled", Toast.LENGTH_SHORT).show()
                onLocationReady()
            } else {
                Toast.makeText(context, "Location is required for tracking", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        // Step 1: Request location permission
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        // Wait until permission granted
        val hasPermission = activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return@LaunchedEffect

        // Step 2: Check GPS
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            onLocationReady() // GPS already enabled
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                gpsLauncher.launch(intentSenderRequest)
            }
        }
    }
}
