package com.example.styleap.data

import com.example.styleap.data.remote.dto.ApiWithdrawalResponseDto
import com.example.styleap.data.remote.dto.RewardDto
import com.example.styleap.util.Resource

interface UserRepository {
    suspend fun getUser(id: String): Resource<User>

    suspend fun createUser(user: User): Resource<User>

    suspend fun updateUser(user: User): Resource<User>

    suspend fun deleteUser(id: String): Resource<Unit>

    suspend fun getCurrentUserData(): Resource<User>

    suspend fun updateUsername(username: String): Resource<Unit>

    suspend fun updatePoints(points: Int): Resource<Unit>

    suspend fun incrementPoints(pointsToAdd: Int): Resource<User>

    suspend fun addPointsForAd(pointsToAdd: Int): Resource<User>

    suspend fun withdrawPoints(pointsToWithdraw: Int, withdrawalDetails: Map<String, String>): Resource<ApiWithdrawalResponseDto>

    suspend fun updateUserLevel(newLevel: Int): Resource<Unit>

    suspend fun getAwardedRewards(): Resource<List<RewardDto>>

    suspend fun getAvailableRewards(): Resource<List<RewardDto>>

    suspend fun claimReward(rewardId: String): Resource<Unit>

    suspend fun activatePremium(userId: String, purchaseToken: String): Resource<Unit>
} 