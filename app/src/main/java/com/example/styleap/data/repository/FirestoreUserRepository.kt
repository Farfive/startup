package com.example.styleap.data.repository

import com.example.styleap.data.model.User
import com.example.styleap.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    // Fetches user data as a Flow, emitting updates in real-time
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getUserData(): Flow<User?> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            Timber.w("No authenticated user found, cannot fetch user data.")
            trySend(null) // Emit null if no user is logged in
            awaitClose { } // Close the flow
            return@callbackFlow
        }

        val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)

        val listenerRegistration = userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Error listening to user data for userId: $userId")
                close(error) // Close the flow with an error
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    val user = snapshot.toObject(User::class.java) //.also { it?.uid = userId } // Ensure UID is set
                    Timber.d("User data fetched/updated: $user")
                    trySend(user).isSuccess // isSuccess helps handle backpressure/closure
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing user data for userId: $userId")
                    // Decide how to handle parsing errors, maybe emit null or close with error
                    trySend(null) // Emit null on parsing error for now
                }
            } else {
                Timber.w("User document does not exist for userId: $userId")
                trySend(null) // Emit null if the document doesn't exist
            }
        }

        // Unregister the listener when the flow is cancelled
        awaitClose {
            Timber.d("Closing Firestore listener for userId: $userId")
            listenerRegistration.remove()
        }
    }

    // Saves user data (used during registration)
    suspend fun saveUserData(user: User): Result<Unit> {
        return try {
            if (user.uid.isBlank()) {
                 return Result.failure(IllegalArgumentException("User UID cannot be blank"))
            }
            firestore.collection(USERS_COLLECTION).document(user.uid)
                .set(user) // Use set to create or overwrite
                .await()
            Timber.d("User data saved successfully for uid: ${user.uid}")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Error saving user data for uid: ${user.uid}")
            Result.failure(e)
        }
    }

    // A more generic update function might be useful later
    override suspend fun updateUserData(userId: String, updates: Map<String, Any>): Result<Unit> {
         return try {
            if (userId.isEmpty()) {
                return Result.failure(IllegalArgumentException("User UID cannot be empty"))
            }
            firestore.collection(USERS_COLLECTION).document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Log error
            Result.failure(e)
        }
    }

    override suspend fun purchasePremium(): Result<Boolean> {
        val userId = getCurrentUserId()
        if (userId == null) {
            Timber.w("Cannot purchase premium: No authenticated user.")
            return Result.failure(IllegalStateException("User not logged in."))
        }

        // In a real app, this might involve checking points, calling a Cloud Function,
        // or interacting with a payment gateway.
        // For now, just update the premiumStatus field.
        return try {
            val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)
            // Use update for specific field change, or merge for adding/updating fields
            userDocRef.update("premiumStatus", true).await()
            // Optionally, you could deduct points here too:
            // userDocRef.update("points", FieldValue.increment(-POINTS_FOR_PREMIUM)).await()
            Timber.i("User $userId premium status updated to true.")
            Result.success(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Error updating premium status for user $userId")
            Result.failure(Exception("Failed to update premium status.", e))
        }
    }
} 