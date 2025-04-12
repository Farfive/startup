package com.example.dream.data.repository

import android.util.Log // For logging errors
import com.example.dream.data.model.Season
import com.example.dream.data.model.SeasonConstants
import com.example.dream.data.model.UserSeasonProgress
import com.example.dream.utils.Constants // Assuming your user collection name is here
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirestoreSeasonRepo" // For Logging

@Singleton // Make this a singleton if appropriate for your app
class FirestoreSeasonRepository @Inject constructor(
    private val firestore: FirebaseFirestore // Inject Firestore instance
) : SeasonRepository {

    override suspend fun getAllSeasons(): List<Season> {
        return try {
            val snapshot = firestore.collection(SeasonConstants.SEASONS_COLLECTION)
                .get()
                .await() // Use await() for suspend functions
            snapshot.toObjects(Season::class.java) // Convert query results to Season objects
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all seasons", e)
            emptyList() // Return empty list on error
        }
    }

    // Helper to get the path to the UserSeasonProgress document
    private fun getUserSeasonProgressDocRef(userId: String, seasonId: String) =
        firestore.collection(Constants.USERS_COLLECTION) // Assuming USERS_COLLECTION is in your main Constants
            .document(userId)
            .collection(SeasonConstants.USER_SEASON_PROGRESS_SUBCOLLECTION)
            .document(seasonId)


    override suspend fun getUserSeasonProgress(userId: String, seasonId: String): UserSeasonProgress? {
        if (userId.isBlank() || seasonId.isBlank()) {
            Log.w(TAG, "getUserSeasonProgress called with blank userId or seasonId")
            return null
        }
        return try {
            val document = getUserSeasonProgressDocRef(userId, seasonId).get().await()
            document.toObject(UserSeasonProgress::class.java) // Returns null if document doesn't exist
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user season progress for user $userId, season $seasonId", e)
            null // Return null on error
        }
    }

    override suspend fun saveUserSeasonProgress(progress: UserSeasonProgress): Boolean {
        if (progress.userId.isBlank() || progress.seasonId.isBlank()) {
             Log.w(TAG, "saveUserSeasonProgress called with blank userId or seasonId in progress object")
            return false
        }
        return try {
            // Use set with merge to create or update fields without overwriting the whole doc unless needed
            // Using the specific document ID (seasonId)
            getUserSeasonProgressDocRef(progress.userId, progress.seasonId)
                .set(progress, SetOptions.merge()) // Merge updates existing fields, creates if not exists
                .await()
            true // Indicate success
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user season progress for user ${progress.userId}, season ${progress.seasonId}", e)
            false // Indicate failure
        }
    }

    override suspend fun getUserProgressForEndedSeason(userId: String, endedSeasonId: String): UserSeasonProgress? {
        // Implementation is identical to getUserSeasonProgress for Firestore structure
        return getUserSeasonProgress(userId, endedSeasonId)
    }
} 