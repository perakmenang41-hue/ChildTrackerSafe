package com.example.kidtracker.screen

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.kidtracker.location.ChildLocationService

// ---------------- Permission Handler ----------------
@Composable
fun LocationPermissionHandler(
    context: Context = LocalContext.current,
    onPermissionsGranted: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissions = mutableListOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }.toTypedArray()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val allGranted = perms.values.all { it }
        if (allGranted) {
            onPermissionsGranted()
        } else {
            Toast.makeText(context, "Location permission is required!", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }
}

// ---------------- Main Screen ----------------
@Composable
fun MainScreen() {
    val context = LocalContext.current

    // Handle permissions and start service
    LocationPermissionHandler(context = context) {
        val serviceIntent = Intent(context, ChildLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    // UI
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tracking will start automatically once permissions are granted")
    }
}
