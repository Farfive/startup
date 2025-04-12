package com.example.dream.domain.service

import android.content.SharedPreferences
import android.util.Log
import com.example.styleap.data.model.Season
import com.example.styleap.data.model.SeasonConstants
import com.example.styleap.data.model.UserSeasonProgress
import com.example.dream.data.repository.SeasonRepository
import com.example.styleap.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SeasonService"
private const val PREF_LAST_ACTIVE_SEASON_PREFIX = "last_active_season_"
private const val PREF_LAST_PROCESSED_TRANSITION_PREFIX = "last_processed_transition_"
private const val CACHE_DURATION_MS = 60 * 60 * 1000 // Cache active season for 1 hour

@Singleton // This service likely holds state (cache) and manages core logic
class SeasonService @Inject constructor(
    private val seasonRepository: SeasonRepository,
    private val userRepository: UserRepository,
    private val sharedPreferences: SharedPreferences, // Inject SharedPreferences
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO // Inject dispatcher for background tasks
) {

    // --- Cache for Active Season ---
    private var activeSeasonCache: Season? = null
    private var lastCheckTime: Long = 0L

    /**
     * Determines the currently active season, using a cache.
     */
    suspend fun getCurrentActiveSeason(): Season? = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        // Return cached value if still valid and has not ended
        activeSeasonCache?.let { cachedSeason ->
            if (now - lastCheckTime < CACHE_DURATION_MS && Date(now).before(cachedSeason.endDate)) {
                return@withContext cachedSeason
            }
        }

        // Fetch fresh from repository
        val allSeasons = seasonRepository.getAllSeasons()
        val currentDate = Date(now)
        Log.d(TAG, "Fetched ${allSeasons.size} seasons. Current time: $currentDate")

        val activeSeason = allSeasons.find { season ->
            // Check if current date is within the season's start and end date
             currentDate.after(season.startDate) && currentDate.before(season.endDate)
        }

        // Update cache
        activeSeasonCache = activeSeason
        lastCheckTime = now
        Log.d(TAG, "Active season determined: ${activeSeason?.seasonId ?: "None"}")

        return@withContext activeSeason
    }

    /**
     * Records user activity points towards the current active season.
     * Creates or updates the user's season progress document.
     */
    suspend fun recordUserActivity(pointsEarned: Long) = withContext(ioDispatcher) {
        if (pointsEarned <= 0) return@withContext // No points earned
        val userId = userRepository.getCurrentUserId()
        if (userId == null) {
            Log.w(TAG, "Cannot record activity, user not logged in.")
            return@withContext
        }

        val activeSeason = getCurrentActiveSeason()
        if (activeSeason == null) {
            Log.w(TAG, "Cannot record activity, no active season.")
            return@withContext
        }

        // Get or create progress for this season
        var progress = seasonRepository.getUserSeasonProgress(userId, activeSeason.seasonId)
        if (progress == null) {
            Log.d(TAG, "No existing progress for user $userId in season ${activeSeason.seasonId}. Creating new.")
            progress = UserSeasonProgress(
                userId = userId,
                seasonId = activeSeason.seasonId,
                level = SeasonConstants.STARTING_LEVEL,
                pointsInSeason = 0L // Start with 0 points
            )
        } else {
             Log.d(TAG, "Existing progress found for user $userId in season ${activeSeason.seasonId}: Level ${progress.level}, Points ${progress.pointsInSeason}")
        }

        // Update seasonal points
        progress.pointsInSeason += pointsEarned

        // Update level based on points
        val newLevel = calculateLevel(progress.pointsInSeason)
        if (newLevel != progress.level) {
             Log.d(TAG, "User $userId level up in season ${activeSeason.seasonId}: ${progress.level} -> $newLevel")
            progress.level = newLevel
        }

        // Save updated progress
        val success = seasonRepository.saveUserSeasonProgress(progress)
        if (success) {
            Log.d(TAG, "Successfully saved progress for user $userId in season ${activeSeason.seasonId}. New points: ${progress.pointsInSeason}, Level: ${progress.level}")
             // Ensure this season is marked as the last active one
            setLastActiveSeasonIdForUser(userId, activeSeason.seasonId)
        } else {
            Log.e(TAG, "Failed to save progress for user $userId in season ${activeSeason.seasonId}")
        }
    }

    /**
     * Calculates the user's level based on the points earned within a season.
     * Replace this with your actual game/app logic.
     */
    private fun calculateLevel(points: Long): Int {
        // Example: Simple linear progression - Every 100 points = 1 level up
        // Add 1 because level 0 exists. Level 1 is reached at 100 points.
        return (points / 100).toInt()
         // More complex examples:
         // return when {
         //    points < 100 -> 0
         //    points < 300 -> 1
         //    points < 600 -> 2
         //    else -> 3
         // }
    }

    // --- Reset Logic ---

    /**
     * Checks if a season reset needs to be applied for the given user and applies it.
     * Should be called periodically (e.g., on app start).
     */
    suspend fun checkAndApplySeasonReset(userId: String) = withContext(ioDispatcher) {
        Log.d(TAG, "Checking for season reset for user $userId")
        val allSeasons = seasonRepository.getAllSeasons().sortedBy { it.startDate }
        if (allSeasons.isEmpty()) {
             Log.w(TAG, "No seasons defined, cannot perform reset check.")
            return@withContext
        }
        val currentDate = Date()

        val lastActiveSeasonId = getLastActiveSeasonIdForUser(userId)
        val lastActiveSeason = allSeasons.find { it.seasonId == lastActiveSeasonId }

        Log.d(TAG, "User $userId last active season ID: $lastActiveSeasonId")

        // Case 1: User has previous season activity & that season *might* have ended
        if (lastActiveSeason != null) {
            // Check if the last active season has actually ended
            if (lastActiveSeason.endDate.before(currentDate)) {
                Log.d(TAG, "Last active season ${lastActiveSeason.seasonId} has ended.")
                // Find the season immediately following the ended one
                val nextSeason = allSeasons.firstOrNull { it.startDate.after(lastActiveSeason.endDate) }

                if (nextSeason != null) {
                    Log.d(TAG, "Found next season: ${nextSeason.seasonId}")
                    // Check if this specific transition (lastActive -> next) was already processed
                    val transitionKey = "${lastActiveSeason.seasonId}_to_${nextSeason.seasonId}"
                    if (!wasTransitionProcessed(userId, transitionKey)) {
                        Log.i(TAG, "Applying reset logic for transition: $transitionKey for user $userId")
                        applyResetLogic(userId, lastActiveSeason.seasonId, nextSeason.seasonId)
                        saveLastProcessedTransition(userId, transitionKey)
                    } else {
                         Log.d(TAG, "Transition $transitionKey already processed for user $userId.")
                         // Ensure user is marked as active in the next season if they weren't already
                         if (getLastActiveSeasonIdForUser(userId) != nextSeason.seasonId) {
                             setLastActiveSeasonIdForUser(userId, nextSeason.seasonId)
                         }
                    }
                } else {
                    Log.d(TAG, "No subsequent season found after ${lastActiveSeason.seasonId}.")
                    // No next season defined yet, user remains technically associated with the ended one
                }
            } else {
                Log.d(TAG, "Last active season ${lastActiveSeason.seasonId} is still ongoing.")
                // Season is still active, no reset needed right now.
                // Ensure progress exists if somehow deleted
                 ensureProgressExists(userId, lastActiveSeason.seasonId)
            }
        }
        // Case 2: User has no previous recorded season activity (new user or returning after cleanup?)
        else {
            Log.d(TAG, "User $userId has no recorded last active season.")
            // Find the *currently* active season based on date
            val currentActiveSeason = allSeasons.find { currentDate.after(it.startDate) && currentDate.before(it.endDate) }

            if (currentActiveSeason != null) {
                Log.d(TAG, "Found current active season for new/returning user: ${currentActiveSeason.seasonId}")
                // Ensure a progress document exists for this user in the current season
                ensureProgressExists(userId, currentActiveSeason.seasonId, true)
                setLastActiveSeasonIdForUser(userId, currentActiveSeason.seasonId)
            } else {
                Log.d(TAG, "No currently active season found for new/returning user.")
                // Nothing to do, wait for a season to become active
            }
        }
    }

    /** Ensures a UserSeasonProgress document exists, creating a default one if not. */
    private suspend fun ensureProgressExists(userId: String, seasonId: String, isInitial: Boolean = false) {
        val progress = seasonRepository.getUserSeasonProgress(userId, seasonId)
        if (progress == null) {
            Log.i(TAG, "Progress for user $userId season $seasonId missing. Creating default record.")
            val initialProgress = UserSeasonProgress(
                userId = userId,
                seasonId = seasonId,
                level = SeasonConstants.STARTING_LEVEL,
                pointsInSeason = 0
            )
            seasonRepository.saveUserSeasonProgress(initialProgress)
        }
    }


    /** Applies the reset logic based on the user's performance in the ended season. */
    private suspend fun applyResetLogic(userId: String, endedSeasonId: String, newSeasonId: String) {
        val endedProgress = seasonRepository.getUserProgressForEndedSeason(userId, endedSeasonId)
        Log.d(TAG, "Applying reset for user $userId. Ended season $endedSeasonId progress: ${endedProgress?.level ?: "Not Found"}")

        val startingLevelForNewSeason = if (endedProgress != null && endedProgress.level >= SeasonConstants.RESET_THRESHOLD_LEVEL) {
             Log.d(TAG,"User met threshold (${endedProgress.level} >= ${SeasonConstants.RESET_THRESHOLD_LEVEL}). Resetting to level ${SeasonConstants.RESET_TO_LEVEL}")
            SeasonConstants.RESET_TO_LEVEL
        } else {
             Log.d(TAG,"User did not meet threshold (${endedProgress?.level} < ${SeasonConstants.RESET_THRESHOLD_LEVEL}) or no progress found. Resetting to level ${SeasonConstants.STARTING_LEVEL}")
            SeasonConstants.STARTING_LEVEL
        }

        // Create progress record for the new season
        val newSeasonProgress = UserSeasonProgress(
            userId = userId,
            seasonId = newSeasonId,
            level = startingLevelForNewSeason,
            pointsInSeason = 0L // Always reset points
        )
        val success = seasonRepository.saveUserSeasonProgress(newSeasonProgress)
        if (success) {
            // Update user's state to reflect they are now in the new season
            setLastActiveSeasonIdForUser(userId, newSeasonId)
            Log.i(TAG, "Successfully created progress for user $userId in new season $newSeasonId with starting level $startingLevelForNewSeason")
        } else {
             Log.e(TAG, "Failed to create progress for user $userId in new season $newSeasonId")
        }

        // Optional: Archive or delete endedProgress data if needed
    }


    // --- Helper functions for user state using SharedPreferences ---

    private fun getLastActiveSeasonIdForUser(userId: String): String? {
        val key = PREF_LAST_ACTIVE_SEASON_PREFIX + userId
        return sharedPreferences.getString(key, null)
    }

    private fun setLastActiveSeasonIdForUser(userId: String, seasonId: String) {
        val key = PREF_LAST_ACTIVE_SEASON_PREFIX + userId
        sharedPreferences.edit().putString(key, seasonId).apply()
         Log.d(TAG,"Set last active season for user $userId to $seasonId")
    }

    private fun saveLastProcessedTransition(userId: String, transitionKey: String) {
        // Stores "endedSeasonId_to_newSeasonId" -> true
        val key = PREF_LAST_PROCESSED_TRANSITION_PREFIX + userId + "_" + transitionKey
        sharedPreferences.edit().putBoolean(key, true).apply()
        Log.d(TAG,"Saved processed transition $transitionKey for user $userId")
    }

    private fun wasTransitionProcessed(userId: String, transitionKey: String): Boolean {
        val key = PREF_LAST_PROCESSED_TRANSITION_PREFIX + userId + "_" + transitionKey
        return sharedPreferences.getBoolean(key, false)
    }
} 