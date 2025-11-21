package com.example.kidtracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidtracker.system.DataStoreManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginChildState {
    object Idle : LoginChildState()
    object Loading : LoginChildState()
    data class Success(val childUID: String) : LoginChildState()
    data class Error(val message: String) : LoginChildState()
}

class LoginChildViewModel(
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow<LoginChildState>(LoginChildState.Idle)
    val state: StateFlow<LoginChildState> = _state

    fun loginChild(email: String, uid: String) {
        _state.value = LoginChildState.Loading

        val queryEmail = email.trim()
        val queryUID = uid.trim()

        firestore.collection("registered_users") // ✅ correct
            .whereEqualTo("email", queryEmail)
            .whereEqualTo("uniqueId", queryUID)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val child = result.documents[0]

                    val childName = child.getString("childName") ?: "Child"
                    val childUID = child.getString("uniqueId") ?: ""
                    val childEmail = child.getString("email") ?: ""

                    // Save to DataStore
                    viewModelScope.launch {
                        dataStore.saveChildName(childName)
                        dataStore.saveChildUID(childUID)
                        dataStore.saveChildEmail(childEmail)
                    }

                    _state.value = LoginChildState.Success(childUID)

                } else {
                    _state.value = LoginChildState.Error("Invalid email or UID")
                }
            }
            .addOnFailureListener {
                _state.value = LoginChildState.Error("Login failed. Check your internet connection.")
            }
    }
}
