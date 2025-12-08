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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.example.kidtracker.models.RegisterRequest
import com.example.kidtracker.system.RetrofitInstance

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

    val countries = java.util.Locale.getISOCountries().map { code ->
        java.util.Locale("", code).displayCountry
    }.sorted()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Register Child", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = name, onValueChange = { name = it }, label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = age, onValueChange = { age = it }, label = { Text("Age") },
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

        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        OutlinedTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it },
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

                scope.launch {
                    try {
                        val request = RegisterRequest(
                            name = name.trim(),
                            email = email.trim(),
                            age = age.trim(),
                            country = country.trim(),
                            password = password.trim()
                        )

                        Log.d("REGISTER_API", "Sending request: $request")

                        val response = RetrofitInstance.api.registerUser(request)

                        Log.d("REGISTER_API", "Response Code: ${response.code()} Body: ${response.body()}")

                        if (response.isSuccessful && response.body()?.success == true) {
                            val uid = response.body()?.uniqueId ?: ""

                            // Save child info locally
                            dataStoreManager.saveChildName(name)
                            dataStoreManager.saveChildEmail(email)
                            dataStoreManager.saveChildUID(uid)

                            // ✅ Save in Firestore under "registered_users"
                            val childData = hashMapOf(
                                "uniqueId" to uid,          // ✅ Field used by parent to check
                                "name" to name.trim(),
                                "email" to email.trim(),
                                "age" to age.trim(),
                                "password" to password.trim(),
                                "country" to country.trim(),
                                "createdAt" to com.google.firebase.Timestamp.now()
                            )

                            firestore.collection("registered_users")
                                .document(uid)              // Document ID is UID
                                .set(childData)
                                .addOnSuccessListener { Log.d("FIRESTORE", "Child saved: $uid") }
                                .addOnFailureListener { e -> Log.e("FIRESTORE", "Failed: ${e.message}") }


                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Registered! UID emailed.", Toast.LENGTH_LONG).show()
                                navController.navigate("login") {
                                    popUpTo("child_register") { inclusive = true }
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, response.body()?.message ?: "Server error", Toast.LENGTH_LONG).show()
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("REGISTER_EXCEPTION", e.stackTraceToString())
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } finally {
                        loading = false
                    }
                }
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
