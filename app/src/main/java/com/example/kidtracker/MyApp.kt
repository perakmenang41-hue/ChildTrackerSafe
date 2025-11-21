package com.example.kidtracker

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Correct Firebase initialization
        FirebaseApp.initializeApp(this)
    }
}