package com.example.kidtracker.activity

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kidtracker.viewmodel.RegisterViewModel
import com.example.kidtracker.viewmodel.RegisterViewModelState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegisterScreen(registerViewModel: RegisterViewModel = viewModel()) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Observe ViewModel state
    LaunchedEffect(Unit) {
        registerViewModel.state.collectLatest { state ->
            when (state) {
                is RegisterViewModelState.Loading -> isLoading = true
                is RegisterViewModelState.Success -> {
                    isLoading = false
                    Toast.makeText(
                        context,
                        "Registration successful! UID: ${state.uniqueId}",
                        Toast.LENGTH_LONG
                    ).show()
                    // Clear fields
                    name = ""
                    email = ""
                    age = ""
                    country = ""
                    password = ""
                    confirmPassword = ""
                }
                is RegisterViewModelState.Error -> {
                    isLoading = false
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                else -> isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("Country") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = {
                if (name.isBlank() || age.isBlank() || country.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (password != confirmPassword) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
                    return@Button
                }

                registerViewModel.registerUser(
                    name = name.trim(),
                    email = email.trim(),
                    age = age.trim(),
                    country = country.trim(),
                    password = password.trim()
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Registering..." else "Register")
        }
    }
}
