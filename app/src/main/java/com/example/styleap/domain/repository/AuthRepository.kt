package com.example.styleap.domain.repository

import com.example.styleap.data.model.UserType

// Authentication Repository Interface
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Boolean> // Returns Result of success
    suspend fun register(name: String, email: String, password: String, userType: UserType): Result<Boolean>
    suspend fun logout() // Add logout method
    // Optional: Add getCurrentUserId(): String?, isLoggedIn(): Flow<Boolean>
} 