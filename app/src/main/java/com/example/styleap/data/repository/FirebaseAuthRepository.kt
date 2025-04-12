package com.example.styleap.data.repository

import com.example.styleap.data.model.User
import com.example.styleap.data.model.UserType
import com.example.styleap.domain.repository.AuthRepository
import com.example.styleap.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: FirebaseAuthException) {
            Timber.e(e, "Firebase login failed")
            // Provide more specific error messages based on e.errorCode if desired
            Result.failure(mapFirebaseAuthException(e))
        } catch (e: CancellationException) {
            throw e // Re-throw cancellation exceptions
        } catch (e: Exception) {
            Timber.e(e, "Generic login failed")
            Result.failure(Exception("Login failed. Please try again.", e))
        }
    }

    override suspend fun register(name: String, email: String, password: String, userType: UserType): Result<Boolean> {
         return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                // Create User object
                val newUser = User(
                    uid = firebaseUser.uid,
                    username = name,
                    email = email,
                    userType = userType
                    // Default values for points, level, premiumStatus will be used
                )
                // Save user data to Firestore
                val saveResult = userRepository.saveUserData(newUser)
                if (saveResult.isFailure) {
                    // Log the error, potentially roll back or notify user?
                    Timber.e(saveResult.exceptionOrNull(), "Failed to save user data for ${firebaseUser.uid}")
                    // Decide if registration should still be considered successful
                    // For now, we continue, but you might want to handle this differently
                }
                Timber.d("User registered and data saved: ${firebaseUser.uid}, Name: $name, Type: $userType")
                Result.success(true)
            } else {
                Timber.e("Firebase user was null after registration")
                Result.failure(Exception("Registration failed: User creation returned null."))
            }
        } catch (e: FirebaseAuthException) {
            Timber.e(e, "Firebase registration failed")
            Result.failure(mapFirebaseAuthException(e))
        } catch (e: CancellationException) {
            throw e // Re-throw cancellation exceptions
        } catch (e: Exception) {
            Timber.e(e, "Generic registration failed")
            Result.failure(Exception("Registration failed. Please try again.", e))
        }
    }

    override suspend fun logout() {
        try {
            firebaseAuth.signOut()
            Timber.i("User logged out successfully.")
            // No result needed, failure is indicated by exception (though signOut doesn't typically throw)
        } catch (e: Exception) {
            // This is unlikely for signOut, but handle defensively
            Timber.e(e, "Error signing out")
            // Re-throw or handle as appropriate for your app's error strategy
            // throw Exception("Logout failed.", e)
        }
    }

    // Helper to map Firebase exceptions to more user-friendly messages
    private fun mapFirebaseAuthException(e: FirebaseAuthException): Exception {
        return when (e.errorCode) {
            "ERROR_INVALID_CREDENTIAL", "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD" ->
                Exception("Invalid email or password.", e)
            "ERROR_EMAIL_ALREADY_IN_USE" ->
                Exception("This email address is already in use.", e)
            "ERROR_WEAK_PASSWORD" ->
                Exception("Password is too weak. Please use at least 6 characters.", e)
            "ERROR_INVALID_EMAIL" ->
                Exception("Please enter a valid email address.", e)
             "ERROR_NETWORK_REQUEST_FAILED" ->
                Exception("Network error. Please check your connection.", e)
            // Add other specific error codes as needed
            else ->
                Exception("An authentication error occurred. [${e.errorCode}]", e)
        }
    }
} 