package com.example.styleap.data

import com.example.styleap.util.Resource // Assuming Resource class for state handling
import com.google.firebase.auth.FirebaseUser

// Define data class for registration parameters if not already defined elsewhere
data class RegistrationParams(
    val name: String,
    val email: String,
    val password: String,
    val userType: UserType // Use the existing UserType enum
)

// Define data class for registration result
data class RegistrationResult(
    val uid: String
)

// Define data class for login parameters
data class LoginParams(
    val email: String,
    val password: String
)

// Define data class for login result (could just be the FirebaseUser)
data class LoginResult(
    val user: FirebaseUser
)

interface AuthRepository {
    suspend fun registerUser(params: RegistrationParams): Resource<RegistrationResult>
    suspend fun loginUser(params: LoginParams): Resource<LoginResult>
    fun getCurrentUser(): FirebaseUser?
    fun logoutUser()
} 