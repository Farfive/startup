package com.example.styleap.data.repository

import com.example.styleap.data.model.ProgressData
import com.example.styleap.domain.repository.ProgressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class FirestoreProgressRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val functions: FirebaseFunctions
) : ProgressRepository {

    companion object {
        private const val USER_PROGRESS_COLLECTION = "user_progress"
        private const val AD_REWARD_POINTS = 15 // Example points
        private const val WITHDRAW_POINTS_AMOUNT = 50 // Example amount
        private const val WITHDRAW_MIN_THRESHOLD = 100 // Example minimum points needed to withdraw
    }

    private fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getProgressData(): Flow<ProgressData?> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            Timber.w("No authenticated user, cannot fetch progress data.")
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val progressDocRef = firestore.collection(USER_PROGRESS_COLLECTION).document(userId)

        val listener = progressDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to progress data for userId: $userId")
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                 try {
                    val progress = snapshot.toObject(ProgressData::class.java)
                    // Add logic to calculate progressToNextLevel if needed based on points/level rules
                    Timber.d("Progress data loaded/updated: $progress")
                    trySend(progress).isSuccess
                } catch (e: Exception) {
                     Timber.e(e, "Error parsing progress data for userId: $userId")
                    trySend(null)
                }
            } else {
                 // Document doesn't exist. Create a default one.
                 Timber.w("Progress document does not exist for userId: $userId. Creating default.")
                 val defaultProgress = ProgressData() // Use default values from the constructor
                 progressDocRef.set(defaultProgress, SetOptions.merge()) // Use merge to avoid overwriting if it was created concurrently
                     .addOnSuccessListener {
                         Timber.d("Default progress document created for userId: $userId")
                         // Send the default data into the flow after successful creation
                         trySend(defaultProgress).isSuccess
                     }
                     .addOnFailureListener { e ->
                          Timber.e(e, "Failed to create default progress document for userId: $userId")
                          // Send null or close with error if creation fails
                          trySend(null).isSuccess
                          // close(e)
                     }
            }
        }
        awaitClose { listener.remove() }
    }

    // In a real app, watching an ad and withdrawing points should ideally be handled
    // by Cloud Functions for security (prevent cheating) and potentially complex logic
    // (e.g., checking ad cooldowns, validating withdrawal conditions).
    // The implementations below perform direct Firestore updates for simplicity.

    override suspend fun watchAdForPoints(): Result<Int> {
        val userId = getCurrentUserId()
            ?: return Result.failure(IllegalStateException("User not logged in"))

        return try {
            // Call the Cloud Function 'watchAdReward'
            // Pass any necessary data (e.g., ad context), empty map for now
            val result = functions
                .getHttpsCallable("watchAdReward")
                .call(emptyMap<String, Any>()) // Pass data if needed
                .await()

            // Assuming the function returns { success: true, pointsAwarded: number }
            val pointsAwarded = (result.data as? Map<*, *>)?.get("pointsAwarded") as? Int ?: 0

            Timber.i("Cloud function 'watchAdReward' success for user $userId, points: $pointsAwarded")
            Result.success(pointsAwarded)

        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFunctionsException) {
            Timber.e(e, "Cloud function 'watchAdReward' failed for user $userId. Code: ${e.code}, Details: ${e.details}")
            // Map Firebase Functions exceptions to user-friendly messages
            Result.failure(mapFunctionsException(e))
        } catch (e: Exception) {
            Timber.e(e, "Generic error calling 'watchAdReward' for user $userId")
            Result.failure(Exception("Failed to claim ad reward. Please try again.", e))
        }
    }

    override suspend fun withdrawPoints(): Result<Boolean> {
         val userId = getCurrentUserId()
            ?: return Result.failure(IllegalStateException("User not logged in"))

        return try {
            // Call the Cloud Function 'withdrawPointsRequest'
            // Pass any necessary data (e.g., withdrawal amount, target), empty map for now
            functions
                .getHttpsCallable("withdrawPointsRequest")
                .call(emptyMap<String, Any>()) // Pass data if needed
                .await()

            Timber.i("Cloud function 'withdrawPointsRequest' success for user $userId.")
            Result.success(true)

        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFunctionsException) {
             Timber.e(e, "Cloud function 'withdrawPointsRequest' failed for user $userId. Code: ${e.code}, Details: ${e.details}")
             Result.failure(mapFunctionsException(e))
        } catch (e: Exception) {
            Timber.e(e, "Generic error calling 'withdrawPointsRequest' for user $userId")
            Result.failure(Exception("Withdrawal failed. Please try again.", e))
        }
    }

    // Helper to map Functions exceptions (can be expanded)
    private fun mapFunctionsException(e: FirebaseFunctionsException): Exception {
        val message = e.message ?: "An error occurred."
        // You can customize messages based on e.code (functions.https.HttpsErrorCode)
        return when (e.code) {
            FirebaseFunctionsException.Code.UNAUTHENTICATED -> Exception("Authentication required.", e)
            FirebaseFunctionsException.Code.FAILED_PRECONDITION -> Exception(message, e) // Use message from function if available
            FirebaseFunctionsException.Code.NOT_FOUND -> Exception("Data not found.", e)
            // Add other codes as needed
            else -> Exception("Withdrawal failed: $message", e)
        }
    }
} 