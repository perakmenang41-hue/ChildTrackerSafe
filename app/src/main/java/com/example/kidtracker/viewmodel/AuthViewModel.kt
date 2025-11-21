package com.example.kidtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtracker.models.*
import com.example.kidtracker.system.RetrofitInstance
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    /**
     * Register a user with backend.
     * Backend now requires: name, email, age, country, password
     * Backend generates uniqueId automatically.
     */
    fun registerUser(
        name: String,
        email: String,
        age: String,
        country: String,
        password: String,
        callback: (Boolean, String, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    name = name.trim(),
                    email = email.trim(),
                    age = age.trim(),
                    country = country.trim(),
                    password = password.trim()
                )

                val response = RetrofitInstance.api.registerUser(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val uid = response.body()?.uniqueId ?: ""
                    callback(true, "Registered! UID sent to email.", uid)
                } else {
                    callback(false, response.body()?.message ?: "Registration failed", null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false, "Error: ${e.localizedMessage}", null)
            }
        }
    }

    /**
     * Login user with email and uniqueId
     */
    fun loginUser(
        email: String,
        uniqueId: String,
        callback: (Boolean, String, UserData?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.loginUser(
                    LoginRequest(email.trim(), uniqueId.trim())
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    callback(true, "Login successful", response.body()?.user)
                } else {
                    callback(false, response.body()?.message ?: "Invalid email or UID", null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false, "Error: ${e.localizedMessage}", null)
            }
        }
    }
}
