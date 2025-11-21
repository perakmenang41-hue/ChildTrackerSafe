package com.example.kidtracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kidtracker.screen.ChildRegisterScreen
import com.example.kidtracker.screen.DashboardScreen
import com.example.kidtracker.screen.LoginScreen
import com.example.kidtracker.screen.SplashScreen
import com.example.kidtracker.system.DataStoreManager

@Composable
fun MainNavigation(dataStoreManager: DataStoreManager) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        // Splash Screen
        composable("splash") {
            SplashScreen(navController = navController)
        }

        // Child Registration Screen
        composable("child_register") {
            ChildRegisterScreen(navController, dataStoreManager)
        }


        // Login Screen
        composable("login") {
            LoginScreen(navController = navController, dataStoreManager = dataStoreManager)
        }

        // Dashboard Screen
        composable(
            route = "dashboard/{childUid}",
            arguments = listOf(navArgument("childUid") { type = NavType.StringType })
        ) { backStackEntry ->
            val childUid = backStackEntry.arguments?.getString("childUid") ?: ""
            DashboardScreen(
                childUid = childUid,
                dataStoreManager = dataStoreManager,
                navController = navController
            )
        }
    }
}
