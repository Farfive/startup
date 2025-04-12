package com.example.styleap.data.repository

import com.example.styleap.data.User
import com.example.styleap.data.UserRepository
import com.example.styleap.data.UserType
import com.example.styleap.data.remote.PointsApiService
import com.example.styleap.data.remote.dto.ApiWithdrawalRequestDto
import com.example.styleap.data.remote.dto.ApiWithdrawalResponseDto
import com.example.styleap.data.remote.dto.RewardDto
import com.example.styleap.data.remote.dto.UserDto
import com.example.styleap.util.Resource
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber
import javax.inject.Inject
import kotlin.Exception
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import android.util.Log
import com.android.billingclient.api.*

// TODO: Implement mappers between User (domain) and UserDto (network)
// For simplicity, this example might use User directly if fields match,
// but mapping is recommended for clean architecture.
// fun UserDto.toUser(): User { ... }
// fun User.toUserDto(): UserDto { ... }

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth, // Keep to easily get current UID
    private val apiService: PointsApiService // Inject the API service
) : UserRepository {

    // Helper to get current user ID safely
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Helper to handle API responses
    private suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): Resource<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("API returned empty body")
            } else {
                Timber.e("API Error: Code=${response.code()}, Message=${response.message()}, ErrorBody=${response.errorBody()?.string()}")
                Resource.Error("API Error ${response.code()}: ${response.message()}")
            }
        } catch (e: HttpException) {
            Timber.e(e, "Network error (HTTP)")
            Resource.Error("Network error: ${e.message()}")
        } catch (e: IOException) {
            Timber.e(e, "Network error (IO)")
            Resource.Error("Network connection error.")
        } catch (e: Exception) {
            Timber.e(e, "Unknown error during API call")
            Resource.Error("An unexpected error occurred: ${e.message}")
        }
    }

    override suspend fun getUser(id: String): Resource<User> {
         // Assuming UserDto can be mapped or is compatible with User
        return when (val result = safeApiCall { apiService.getUser(id) }) {
            is Resource.Success -> Resource.Success(result.data!!)
            is Resource.Error -> Resource.Error(result.message ?: "Failed to get user")
            else -> Resource.Loading()
        }
    }

    // Note: createUser might not be needed if registration is handled by AuthRepository/Cloud Function
    // If needed, implement similarly using apiService.registerUser
    override suspend fun createUser(user: User): Resource<User> {
         // This depends heavily on your API design for user creation/registration
         // Assuming apiService.registerUser takes a UserDto and returns UserDto
         // return safeApiCall { apiService.registerUser(user.toUserDto()) } // Requires mapping
         Timber.w("createUser in UserRepositoryImpl called - ensure this is intended behavior alongside AuthRepository")
         return Resource.Error("createUser not fully implemented via API") // Placeholder
    }

    override suspend fun updateUser(user: User): Resource<User> {
        // TODO: Replace direct User usage with proper User -> UserDto mapping
        val userDto = UserDto(
            id = user.id, // Assuming User has an id field
            name = user.name, // Assuming User has a name field
            email = user.email, // Assuming User has an email field
            points = user.points, // Assuming User has a points field
            level = user.level, // Assuming User has a level field
            userType = user.userType?.name ?: "Individual", // Still depends on User class having userType
            // Add other relevant fields that need to be sent for update
        )
        return when (val result = safeApiCall { apiService.updateUser(user.id, userDto) }) {
            is Resource.Success -> Resource.Success(result.data!!)
            is Resource.Error -> Resource.Error(result.message ?: "Failed to update user")
            else -> Resource.Loading()
        }
    }

    override suspend fun deleteUser(id: String): Resource<Unit> {
        return safeApiCall { apiService.deleteUser(id) }
    }

    override suspend fun getCurrentUserData(): Resource<User> {
        val userId = getCurrentUserId()
            ?: return Resource.Error("User not authenticated")
        return getUser(userId) // Reuse the getUser implementation
    }

    override suspend fun updateUserLevel(newLevel: Int): Resource<Unit> { // Changed return type
        val userId = getCurrentUserId()
            ?: return Resource.Error("User not authenticated")

        // API needs an endpoint to update *just* the level, or use updateUser
        // This example assumes using updateUser for simplicity, but a dedicated endpoint is better.
        return when(val currentUserData = getCurrentUserData()) {
             is Resource.Success -> {
                 val updatedUser = currentUserData.data!!.copy(level = newLevel)
                  // Assuming updateUser returns Resource<User>
                 when (val updateResult = updateUser(updatedUser)) {
                      is Resource.Success -> Resource.Success(Unit)
                      is Resource.Error -> Resource.Error(updateResult.message ?: "Failed to update level")
                      else -> Resource.Loading() // Or Error
                 }
             }
             is Resource.Error -> Resource.Error(currentUserData.message ?: "Could not get user data to update level")
             else -> Resource.Loading()
        }
    }

    override suspend fun withdrawPoints(pointsToWithdraw: Int, withdrawalDetails: Map<String, String>): Resource<ApiWithdrawalResponseDto> { // Changed signature
        val userId = getCurrentUserId()
            ?: return Resource.Error("User not authenticated for withdrawal.")

        val request = ApiWithdrawalRequestDto(
            userId = userId,
            points = pointsToWithdraw,
            details = withdrawalDetails // e.g., bank account info, payment method
        )
        return safeApiCall { apiService.requestWithdrawal(request) }
    }

    // --- Reward Methods --- (Added based on user request)

    override suspend fun getAwardedRewards(): Resource<List<RewardDto>> {
        val userId = getCurrentUserId()
            ?: return Resource.Error("User not authenticated")
        return safeApiCall { apiService.getAwardedRewards(userId) }
    }

    override suspend fun getAvailableRewards(): Resource<List<RewardDto>> {
        val userId = getCurrentUserId()
            ?: return Resource.Error("User not authenticated")
        return safeApiCall { apiService.getAvailableRewards(userId) }
    }

    override suspend fun claimReward(rewardId: String): Resource<Unit> {
        val userId = getCurrentUserId()
            ?: return Resource.Error("User not authenticated")
        return safeApiCall { apiService.claimReward(userId, rewardId) }
    }

     // --- Point Update Methods (Example - Needs API Endpoints) --- 

     // It's generally better to have specific API endpoints than getting/updating the whole user object

    override suspend fun incrementPoints(pointsToAdd: Int): Resource<User> {
        val userId = getCurrentUserId() ?: return Resource.Error("User not authenticated")
        return when(val currentUserData = getCurrentUserData()) {
            is Resource.Success -> {
                val currentPoints = currentUserData.data?.points ?: 0
                val updatedUser = currentUserData.data!!.copy(points = currentPoints + pointsToAdd)
                // Call the main updateUser method
                updateUser(updatedUser)
            }
            is Resource.Error -> Resource.Error(currentUserData.message ?: "Could not get user data to increment points")
            else -> Resource.Loading()
        }
    }

    override suspend fun addPointsForAd(pointsToAdd: Int): Resource<User> {
         // Reusing incrementPoints logic as the action is similar
         return incrementPoints(pointsToAdd)
    }

    // Update this if needed, maybe via updateUser?
    override suspend fun updateUsername(username: String): Resource<Unit> {
         val userId = getCurrentUserId() ?: return Resource.Error("User not authenticated")
         return when(val currentUserData = getCurrentUserData()) {
             is Resource.Success -> {
                 val updatedUser = currentUserData.data!!.copy(name = username)
                 when (val updateResult = updateUser(updatedUser)) {
                      is Resource.Success -> Resource.Success(Unit)
                      is Resource.Error -> Resource.Error(updateResult.message ?: "Failed to update username")
                      else -> Resource.Loading() // Should not happen
                 }
             }
             is Resource.Error -> Resource.Error(currentUserData.message ?: "Could not get user data to update username")
             else -> Resource.Loading()
         }
    }

    // Update this if needed, maybe via updateUser?
    override suspend fun updatePoints(points: Int): Resource<Unit> {
        val userId = getCurrentUserId() ?: return Resource.Error("User not authenticated")
        return when(val currentUserData = getCurrentUserData()) {
            is Resource.Success -> {
                val updatedUser = currentUserData.data!!.copy(points = points)
                when (val updateResult = updateUser(updatedUser)) {
                    is Resource.Success -> Resource.Success(Unit)
                    is Resource.Error -> Resource.Error(updateResult.message ?: "Failed to update points")
                    else -> Resource.Loading() // Should not happen
                }
            }
            is Resource.Error -> Resource.Error(currentUserData.message ?: "Could not get user data to update points")
            else -> Resource.Loading()
        }
    }

    override suspend fun activatePremium(userId: String, purchaseToken: String): Resource<Unit> {
        // Create a data object with purchase token and other information needed for verification
        val purchaseData = mapOf(
            "purchaseToken" to purchaseToken,
            "productId" to "premium_subscription_monthly", // TODO: Consider making this dynamic if you have multiple products
            "packageName" to "com.example.styleap", // Changed to match the current package name
            // The backend should ideally use the purchase timestamp from Google, not rely on this client timestamp
            // "timestamp" to System.currentTimeMillis() 
        )

        // Call API to activate premium and verify the purchase on the backend
        // The backend is responsible for verifying the token with Google Play Developer API 
        // and updating the user's status in the database if verification succeeds.
        return safeApiCall { apiService.activatePremium(userId, purchaseData) }
    }
} 