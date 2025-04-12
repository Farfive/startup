package com.example.dream.data.repository

import com.example.dream.data.model.Season
import com.example.dream.data.model.UserSeasonProgress

/**
 * Interface for accessing season-related data.
 */
interface SeasonRepository {

    /**
     * Fetches all defined Season documents.
     * @return A list of Season objects or an empty list if none found or error occurs.
     */
    suspend fun getAllSeasons(): List<Season>

    /**
     * Fetches a specific user's progress for a given season.
     * @param userId The ID of the user.
     * @param seasonId The ID of the season.
     * @return The UserSeasonProgress object or null if not found or error occurs.
     */
    suspend fun getUserSeasonProgress(userId: String, seasonId: String): UserSeasonProgress?

    /**
     * Saves or updates a user's season progress. Creates the document if it doesn't exist,
     * otherwise overwrites it (or merges, depending on implementation).
     * @param progress The UserSeasonProgress object to save.
     * @return True if the operation was successful, false otherwise.
     */
    suspend fun saveUserSeasonProgress(progress: UserSeasonProgress): Boolean

    /**
     * Fetches a user's progress specifically for a season that has already ended.
     * This is often the same implementation as getUserSeasonProgress but named for clarity
     * in the context of the reset logic.
     * @param userId The ID of the user.
     * @param endedSeasonId The ID of the ended season.
     * @return The UserSeasonProgress object or null if not found or error occurs.
     */
    suspend fun getUserProgressForEndedSeason(userId: String, endedSeasonId: String): UserSeasonProgress?
} 