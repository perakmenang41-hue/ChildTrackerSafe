package com.example.kidtracker.screen

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kidtracker.location.ChildLocationService
import com.example.kidtracker.system.DataStoreManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, dataStoreManager: DataStoreManager) {
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Child Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = "Toggle Password Visibility")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Login Button
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    error = "Please enter email and password"
                    return@Button
                }

                loading = true
                error = ""

                firestore.collection("registered_users")
                    .whereEqualTo("email", email.trim())
                    .get()
                    .addOnSuccessListener { result ->
                        loading = false
                        if (result.isEmpty) {
                            error = "Email not registered"
                        } else {
                            val userDoc = result.documents[0]
                            val storedPassword = userDoc.getString("password") ?: ""
                            if (storedPassword == password.trim()) {
                                val childName = userDoc.getString("name") ?: "Child"
                                val childUid = userDoc.getString("uniqueId") ?: ""
                                val childEmail = userDoc.getString("email") ?: ""

                                // Save to DataStore
                                scope.launch {
                                    dataStoreManager.saveChildName(childName)
                                    dataStoreManager.saveChildUID(childUid)
                                    dataStoreManager.saveChildEmail(childEmail)
                                }

                                // Start Location Service
                                val intent = Intent(context, ChildLocationService::class.java)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent)
                                } else {
                                    context.startService(intent)
                                }

                                // Navigate to Dashboard
                                navController.navigate("dashboard/$childUid") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                error = "Incorrect password"
                            }
                        }
                    }
                    .addOnFailureListener {
                        loading = false
                        error = "Login failed. Check your internet connection"
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !loading
        ) {
            Text(if (loading) "Logging in..." else "Login")
        }

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("child_register") }) {
            Text("Don't have an account? Register here")
        }
    }
}
