package com.example.dream

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DreamApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Existing application setup code
    }
} 