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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.kidtracker.screen.InboxScreen
import com.example.kidtracker.child.PatternSequencingGameScreen
import com.example.kidtracker.child.MotionSensorService
import com.example.kidtracker.child.TapTheMoleGameScreen
import com.example.kidtracker.location.ChildLocationService
import com.example.kidtracker.screen.ChildRegisterScreen
import com.example.kidtracker.screen.LoginScreen
import com.example.kidtracker.screen.DashboardScreen
import com.example.kidtracker.screen.MemoryGameScreen
import com.example.kidtracker.system.DataStoreManager

class MainActivity : ComponentActivity() {

    private lateinit var dataStoreManager: DataStoreManager

    // --- Launcher for foreground location permission ---
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
            startChildLocationService() // start services after permission granted
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataStoreManager = DataStoreManager(this)

        // Request location permission at app start
        requestLocationPermission()

        setContent {
            val navController = rememberNavController()
            KidTrackerNavHost(navController = navController, dataStoreManager = dataStoreManager)
        }
    }

    // --- Request foreground location permission ---
    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // --- Start the ChildLocationService ---
    private fun startChildLocationService() {
        val intent = Intent(this, ChildLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        // --- Start MotionSensorService at the same time with default UID (optional, can override after login) ---
        startMotionSensorService()
    }

    // --- Start the MotionSensorService (default, will override after login) ---
    private fun startMotionSensorService() {
        val intent = Intent(this, MotionSensorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    // --- Call this function after login with the actual childUID ---
    fun startMotionService(childUID: String) {
        val intent = Intent(this, MotionSensorService::class.java)
        intent.putExtra("childUID", childUID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

// --- NavHost ---
@Composable
fun KidTrackerNavHost(
    navController: NavHostController,
    dataStoreManager: DataStoreManager
) {
    NavHost(
        navController = navController,
        startDestination = "child_register"
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
        // --- Memory Game route ---
        composable("memory_game") {
            MemoryGameScreen(
                dataStoreManager = dataStoreManager, // pass it here
                onExit = { navController.popBackStack() }
            )
        }
        // --- Pattern Sequencing Game route ---
        composable("pattern_game") {
            PatternSequencingGameScreen(
                dataStoreManager = dataStoreManager, // pass it here
                onExit = { navController.popBackStack() }
            )
        }
        // --- Tap The Mole ---
        composable("tap_the_mole") {
            TapTheMoleGameScreen(
                dataStoreManager = dataStoreManager,
                onExit = { navController.popBackStack() }
            )
        }
        // --- Inbox ---
        composable("inbox") {
            InboxScreen(dataStoreManager)
        }

    }
}
