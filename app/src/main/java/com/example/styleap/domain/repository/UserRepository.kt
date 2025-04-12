package com.example.styleap.domain.repository

import com.example.styleap.data.model.User
import kotlinx.coroutines.flow.Flow

// Example placeholder for User Repository
interface UserRepository {
    suspend fun getUserData(): Flow<User?> // Maybe change to getUserData(userId: String)?
    suspend fun purchasePremium(): Result<Boolean>
    suspend fun saveUserData(user: User): Result<Unit>
    // Add methods like updateUserData(user: User), getCurrentUserId(): String?
    fun getCurrentUserId(): String? // Added based on SeasonService usage
} 