package com.example.dream.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Tracks a user's progress within a specific season.
 * This might be stored as a subcollection under users
 * (e.g., users/{userId}/seasonProgress/{seasonId})
 * or as a top-level collection.
 */
data class UserSeasonProgress(
    // These fields might not be needed if the document ID structure includes them,
    // but can be useful for querying if stored in a top-level collection.
    val userId: String = "",
    val seasonId: String = "",

    var level: Int = 0,             // User's calculated level within this season
    var pointsInSeason: Long = 0L,  // Points earned ONLY during this season

    @ServerTimestamp // Firestore automatically sets server time on write/update
    val lastUpdated: Date? = null   // Useful for tracking activity or ordering
    // Optional: Track specific seasonal quests completed, etc.
    // val questsCompleted: List<String> = emptyList()
) 