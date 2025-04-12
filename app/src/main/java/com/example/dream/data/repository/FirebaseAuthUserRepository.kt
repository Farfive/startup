package com.example.dream.data.repository

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Make this a singleton if appropriate
class FirebaseAuthUserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth // Inject FirebaseAuth instance
) : UserRepository {

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    // Implement other methods like getUserProfile if needed, potentially
    // fetching data from Firestore based on the currentUserId()
} 