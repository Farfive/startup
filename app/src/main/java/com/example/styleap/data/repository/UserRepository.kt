package com.example.styleap.data.repository

import com.example.styleap.data.model.User

interface UserRepository {
    suspend fun getUserData(): User?
    suspend fun saveUserData(user: User): Result<Unit>
    suspend fun updateUserData(userId: String, updates: Map<String, Any>): Result<Unit>
    suspend fun purchasePremium(): Result<Unit>
    fun getCurrentUserId(): String?
} 