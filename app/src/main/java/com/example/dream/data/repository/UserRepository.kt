package com.example.dream.data.repository

/**
 * Interface for accessing user-related data, primarily authentication status.
 */
interface UserRepository {

    /**
     * Gets the unique ID of the currently authenticated user.
     * @return The user's UID string, or null if no user is currently signed in.
     */
    fun getCurrentUserId(): String?

    /**
     * Checks if a user is currently signed in.
     * @return True if a user is signed in, false otherwise.
     */
    fun isUserLoggedIn(): Boolean

    // Add other user-related methods if needed, e.g.:
    // suspend fun getUserProfile(userId: String): UserProfile?
    // suspend fun updateUserProfile(userId: String, profile: UserProfile): Boolean
} 