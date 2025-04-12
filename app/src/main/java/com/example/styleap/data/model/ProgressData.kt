package com.example.styleap.data.model

import java.util.Date // Import Date

// Placeholder Data Class
data class ProgressData(
    val username: String = "",
    val points: Int = 0,
    val level: Int = 0,
    val progressToNextLevel: Float = 0.0f, // 0.0 to 1.0
    val isAdAvailable: Boolean = true, // Example: based on time/limits
    val isWithdrawEnabled: Boolean = true, // Example: based on points threshold
    var adLastWatchedTimestamp: Date? = null // Timestamp of the last ad watch
) {
    // Add a no-argument constructor for Firestore deserialization
    constructor() : this("", 0, 0, 0.0f, true, true, null)
} 