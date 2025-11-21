package com.example.kidtracker.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// Create DataStore instance
private val Context.dataStore by preferencesDataStore(name = "kidtracker_prefs")

