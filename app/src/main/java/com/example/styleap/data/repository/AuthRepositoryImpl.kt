package com.example.styleap.data.repository

import com.example.styleap.data.AuthRepository
import com.example.styleap.data.LoginParams
import com.example.styleap.data.LoginResult
import com.example.styleap.data.RegistrationParams
import com.example.styleap.data.RegistrationResult
import com.example.styleap.data.UserType
import com.example.styleap.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException
import java.io.IOException
import com.example.styleap.data.remote.PointsApiService
import com.example.styleap.data.remote.dto.UserDto

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth, // Injected via Hilt
    private val apiService: PointsApiService // Inject PointsApiService
) : AuthRepository {

    override suspend fun registerUser(params: RegistrationParams): Resource<RegistrationResult> {
        return try {
            // Prepare data for the API call
            val data = hashMapOf(
                "email" to params.email,
                "password" to params.password,
                "name" to params.name,
                "userType" to params.userType.name
            )

            // Call the API service using Retrofit pattern
            val response = apiService.registerUser(data)
            
            if (response.isSuccessful && response.body() != null) {
                val resultMap = response.body()
                val uid = resultMap?.get("uid") as? String
                
                if (uid != null) {
                    Resource.Success(RegistrationResult(uid))
                } else {
                    Resource.Error("Registration completed but UID was not returned.")
                }
            } else {
                Resource.Error("Registration failed: ${response.errorBody()?.string() ?: response.message()}")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resource.Error(e.message ?: "An unexpected error occurred.")
        }
    }

    override suspend fun loginUser(params: LoginParams): Resource<LoginResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(params.email, params.password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                Resource.Success(LoginResult(firebaseUser))
            } else {
                // This case should ideally not happen if await() succeeds without exception
                Resource.Error("Login successful but user data is null.")
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Resource.Error("Invalid email or user not found.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Resource.Error("Invalid password.")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resource.Error(e.message ?: "An unexpected error occurred during login.")
        }
    }

    override fun getCurrentUser(): com.google.firebase.auth.FirebaseUser? {
        return auth.currentUser
    }

    override fun logoutUser() {
        auth.signOut()
    }

    // Implement other AuthRepository methods here...

} 