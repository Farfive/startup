package com.example.styleap

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject
import androidx.multidex.MultiDexApplication
import com.google.firebase.crashlytics.FirebaseCrashlytics
import android.util.Log

@HiltAndroidApp
class DreamApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Timber logging initialized for DEBUG build.")
        } else {
            // In release builds, plant a tree that logs WARNING or higher to Crashlytics
            // Also enables automatic crash reporting
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
            Timber.plant(CrashReportingTree())
            Timber.i("Crashlytics enabled and Timber logging initialized for RELEASE build.")
        }

        // Other application-level initializations can go here
        // e.g., FirebaseApp.initializeApp(this)
        // Note: Hilt setup is handled by the annotation

        // Example usage within an Activity, ViewModel, or Service
        val apiKey = BuildConfig.DREAM_API_KEY
        val baseUrl = BuildConfig.API_BASE_URL

        if (apiKey.isEmpty()) {
            Timber.e("API Key is missing! Check gradle.properties")
            // Handle missing key error appropriately
        } else {
            // Use the apiKey for your network requests
            Timber.d("Using API Key starting with: ${apiKey.take(4)}...") // Avoid logging the full key
            Timber.d("Using Base URL: $baseUrl")
        }

        // Access debug flag
        if (BuildConfig.DEBUG_MODE) { // Or just use BuildConfig.DEBUG
            Timber.d("Running in debug mode")
        }
    }

    // Custom Timber Tree for Crashlytics Logging in Release Builds
    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Don't log DEBUG or VERBOSE to Crashlytics
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return // Don't send Verbose or Debug logs to crash reporter
            }

            // Log messages of WARNING or higher priority to Crashlytics
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log("Priority=$priority, Tag=$tag: $message")

            // Log exceptions to Crashlytics
            if (t != null) {
                if (priority == Log.ERROR || priority == Log.ASSERT) {
                    // Log ERROR/ASSERT level exceptions as non-fatal crashes
                    crashlytics.recordException(t)
                } else {
                    // For WARNING level throwables, just log the message
                    crashlytics.log("WARN Throwable: $t")
                }
            }
        }
    }
} 