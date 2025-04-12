package com.example.dream.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a single Season definition in Firestore.
 */
data class Season(
    @DocumentId // Maps Firestore document ID to this field
    val seasonId: String = "", // Unique ID (e.g., "season_2024_q3", generated or manual)

    val seasonName: String = "", // e.g., "Summer Sprint '24"
    val startDate: Date = Date(), // Start date/time of the season
    val endDate: Date = Date(),   // End date/time of the season

    // Optional: Add fields for season-specific themes, reward track IDs, etc.
    // val theme: String = "",
    // val rewardTrackId: String = ""
) 