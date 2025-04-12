package com.example.styleap.domain.repository

import com.example.styleap.data.model.ProgressData
import kotlinx.coroutines.flow.Flow

// Placeholder Repository for Progress/Ads
interface ProgressRepository {
    // Combines user data relevant to progress
    suspend fun getProgressData(): Flow<ProgressData?> // Maybe by User ID? getProgressData(userId: String)
    suspend fun watchAdForPoints(): Result<Int> // Returns points earned or error
    suspend fun withdrawPoints(): Result<Boolean> // Returns success/failure
} 