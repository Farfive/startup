package com.example.styleap.data.remote

import com.example.styleap.data.User // Assuming User data class exists here
import com.example.styleap.data.remote.dto.ApiWithdrawalRequestDto
import com.example.styleap.data.remote.dto.ApiWithdrawalResponseDto
import com.example.styleap.data.remote.dto.RewardDto // Assuming Reward DTO
import com.example.styleap.data.remote.dto.UserDto // Assuming a full User DTO for GET/POST/PUT
import com.example.styleap.data.remote.dto.UserPointsDto
import com.example.styleap.data.remote.dto.CompanyDto // Assuming Company DTO
import retrofit2.Response
import retrofit2.http.*

interface PointsApiService {

    // == User Endpoints ==

    @GET("api/user/{userId}")
    suspend fun getUser(@Path("userId") userId: String): Response<UserDto>

    // Registration endpoint
    @POST("api/register")
    suspend fun registerUser(@Body data: HashMap<String, String>): Response<Map<String, Any>>

    @PUT("api/user/{userId}")
    suspend fun updateUser(@Path("userId") userId: String, @Body user: UserDto): Response<UserDto>

    @DELETE("api/user/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String): Response<Unit>

    @GET("api/user/{userId}/points")
    suspend fun getUserPoints(@Path("userId") userId: String): Response<UserPointsDto>

    // == Reward Endpoints ==

    @GET("api/user/{userId}/rewards/awarded")
    suspend fun getAwardedRewards(@Path("userId") userId: String): Response<List<RewardDto>>

    @GET("api/user/{userId}/rewards/available")
    suspend fun getAvailableRewards(@Path("userId") userId: String): Response<List<RewardDto>>

    @POST("api/user/{userId}/rewards/{rewardId}/claim")
    suspend fun claimReward(@Path("userId") userId: String, @Path("rewardId") rewardId: String): Response<Unit>

    // == Withdrawal Endpoints ==

    @POST("api/withdraw/request")
    suspend fun requestWithdrawal(@Body request: ApiWithdrawalRequestDto): Response<ApiWithdrawalResponseDto>

    // == Premium Endpoints ==

    @POST("api/user/{userId}/premium/activate")
    suspend fun activatePremium(
        @Path("userId") userId: String,
        @Body purchaseData: Map<String, Any>
    ): Response<Unit>

    // == Company Endpoints ==

    @GET("api/company/{companyId}")
    suspend fun getCompany(@Path("companyId") companyId: String): Response<CompanyDto>

    @PUT("api/company/{companyId}")
    suspend fun updateCompany(@Path("companyId") companyId: String, @Body company: CompanyDto): Response<CompanyDto>

    // Add other company endpoints if needed (create, update, etc.)

    // Add other general endpoints as needed
} 