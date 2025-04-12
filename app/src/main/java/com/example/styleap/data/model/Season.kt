package com.example.styleap.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Represents a single Season definition
@Entity(tableName = "seasons")
data class Season(
    @PrimaryKey
    @DocumentId
    val seasonId: String = "",

    val seasonName: String = "",
    val startDate: Date = Date(),
    val endDate: Date = Date(),
)

// Tracks a user's progress within a specific season
@Entity(tableName = "user_season_progress", primaryKeys = ["userId", "seasonId"])
data class UserSeasonProgress(
    val userId: String = "",
    val seasonId: String = "",

    var level: Int = 0,
    var pointsInSeason: Long = 0L,

    @ServerTimestamp
    val lastUpdated: Date? = null
) 