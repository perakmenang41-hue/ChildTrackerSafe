package com.example.kidtracker.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtracker.models.RegisterRequest
import com.example.kidtracker.models.RegisterResponse
import com.example.kidtracker.system.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

sealed class RegisterViewModelState {
    object Idle : RegisterViewModelState()
    object Loading : RegisterViewModelState()
    data class Success(val uniqueId: String) : RegisterViewModelState()
    data class Error(val message: String) : RegisterViewModelState()
}

class RegisterViewModel : ViewModel() {

    private val _state = MutableStateFlow<RegisterViewModelState>(RegisterViewModelState.Idle)
    val state: StateFlow<RegisterViewModelState> = _state

    fun registerUser(name: String, email: String, age: String, country: String, password: String) {
        viewModelScope.launch {
            _state.value = RegisterViewModelState.Loading
            try {
                val request = RegisterRequest(
                    name = name,
                    email = email,
                    age = age,
                    country = country,
                    password = password
                )

                val response = RetrofitInstance.api.registerUser(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val uid = response.body()?.uniqueId ?: ""
                    _state.value = RegisterViewModelState.Success(uid)
                } else {
                    _state.value = RegisterViewModelState.Error(
                        response.body()?.message ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _state.value = RegisterViewModelState.Error(e.localizedMessage ?: "Network error")
            }
        }
    }
}