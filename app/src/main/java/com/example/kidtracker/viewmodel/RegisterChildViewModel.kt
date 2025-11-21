package com.example.kidtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtracker.models.RegisterRequest
import com.example.kidtracker.models.RegisterResponse
import com.example.kidtracker.system.DataStoreManager
import com.example.kidtracker.system.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response


sealed class RegisterChildState {
    object Idle : RegisterChildState()
    object Loading : RegisterChildState()
    data class Success(val uniqueId: String) : RegisterChildState()
    data class Error(val message: String) : RegisterChildState()
}

class RegisterChildViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {

    private val _state = MutableStateFlow<RegisterChildState>(RegisterChildState.Idle)
    val state: StateFlow<RegisterChildState> get() = _state

    /**
     * Register child with backend
     * Backend now requires: name, email, age, country, password
     * Backend generates uniqueId automatically
     */
    fun registerChild(
        name: String,
        email: String,
        age: String,
        country: String,
        password: String
    ) {
        viewModelScope.launch {
            _state.value = RegisterChildState.Loading
            try {
                val request = RegisterRequest(
                    name = name.trim(),
                    email = email.trim(),
                    age = age.trim(),
                    country = country.trim(),
                    password = password.trim()
                )

                val response: Response<RegisterResponse> =
                    RetrofitInstance.api.registerUser(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val uid = response.body()?.uniqueId ?: ""

                    // Save info locally
                    dataStoreManager.saveChildName(name)
                    dataStoreManager.saveChildEmail(email)
                    dataStoreManager.saveChildAge(age)
                    dataStoreManager.saveChildCountry(country)
                    dataStoreManager.saveChildUID(uid)

                    _state.value = RegisterChildState.Success(uid)
                } else {
                    _state.value = RegisterChildState.Error(
                        response.body()?.message ?: "Unknown server error"
                    )
                }
            } catch (e: Exception) {
                _state.value = RegisterChildState.Error(
                    e.localizedMessage ?: "Network error"
                )
            }
        }
    }
}
