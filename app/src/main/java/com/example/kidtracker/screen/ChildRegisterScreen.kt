package com.example.kidtracker.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kidtracker.system.DataStoreManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.kidtracker.models.RegisterRequest
import com.example.kidtracker.network.RetrofitClient
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildRegisterScreen(
    navController: NavController,
    dataStoreManager: DataStoreManager
) {

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val countries = Locale.getISOCountries().map { code -> Locale("", code).displayCountry }.sorted()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Register Child", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = country,
                onValueChange = {},
                readOnly = true,
                label = { Text("Country") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                countries.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c) },
                        onClick = { country = c; expanded = false }
                    )
                }
            }
        }

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") },
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

                if (name.isBlank() || age.isBlank() || country.isBlank() ||
                    email.isBlank() || password.isBlank() || confirmPassword.isBlank()
                ) {
                    Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (password != confirmPassword) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                loading = true

                // ================== CALL BACKEND API ==================
                scope.launch {
                    try {
                        // ✅ Create request once
                        val request = RegisterRequest(
                            name = name.trim(),
                            email = email.trim(),
                            age = age.trim(),
                            country = country.trim(),
                            password = password.trim()
                        )

                        // 🔥 DEBUG LOGS HERE — before sending
                        Log.d("REGISTER_API", "Sending request: $request")

                        // ✅ Send request
                        val response = RetrofitClient.apiService.registerUser(request)

                        // 🔥 DEBUG LOG RESPONSE
                        Log.d("REGISTER_API", "Response Code: ${response.code()}")
                        Log.d("REGISTER_API", "Response Body: ${response.body()}")
                        Log.e("REGISTER_ERROR", "Error Body: ${response.errorBody()?.string()}")

                        if (response.isSuccessful && response.body()?.success == true) {

                            val uid = response.body()?.uniqueId ?: ""

                            // Save child info locally
                            dataStoreManager.saveChildName(name)
                            dataStoreManager.saveChildEmail(email)
                            dataStoreManager.saveChildUID(uid)

                            Toast.makeText(context, "Registered! UID emailed.", Toast.LENGTH_LONG).show()

                            navController.navigate("login") {
                                popUpTo("child_register") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                response.body()?.message ?: "Server error",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } catch (e: Exception) {
                        Log.e("REGISTER_EXCEPTION", e.stackTraceToString())
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        loading = false
                    }
                }

                // ================== FIX 4 ==================
                // FirebaseManager.sendUidEmail(...) removed because backend sends email
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Registering..." else "Register")
        }

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login here")
        }
    }
}
