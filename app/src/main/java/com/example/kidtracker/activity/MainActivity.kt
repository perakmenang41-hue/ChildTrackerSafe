package com.example.kidtracker.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.kidtracker.location.ChildLocationService
import com.example.kidtracker.screen.ChildRegisterScreen
import com.example.kidtracker.screen.LoginScreen
import com.example.kidtracker.screen.DashboardScreen
import com.example.kidtracker.system.DataStoreManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

class MainActivity : ComponentActivity() {

    private lateinit var dataStoreManager: DataStoreManager

    // --- NEW: Launcher for foreground location permission ---
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
            startChildLocationService() // <-- start service after permission granted
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataStoreManager = DataStoreManager(this)

        // --- NEW: Request location permission at app start ---
        requestLocationPermission()

        setContent {
            val navController = rememberNavController()
            KidTrackerNavHost(navController = navController, dataStoreManager = dataStoreManager)
        }
    }

    // --- NEW: Function to request foreground location permission ---
    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // --- NEW: Function to start the ChildLocationService ---
    private fun startChildLocationService() {
        val intent = Intent(this, ChildLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

// --- DO NOT CHANGE: NavHost ---
@Composable
fun KidTrackerNavHost(navController: androidx.navigation.NavHostController, dataStoreManager: DataStoreManager) {
    NavHost(
        navController = navController,
        startDestination = "child_register" // first screen
    ) {
        composable("child_register") {
            ChildRegisterScreen(navController, dataStoreManager)
        }
        composable("login") {
            LoginScreen(navController, dataStoreManager)
        }
        composable("dashboard/{childUid}") { backStackEntry ->
            val childUid = backStackEntry.arguments?.getString("childUid") ?: ""
            DashboardScreen(childUid, dataStoreManager, navController)
        }
    }
}
