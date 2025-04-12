package com.example.styleap.data.remote.dto

data class ApiWithdrawalResponseDto(
    val id: String,
    val userId: String,
    val points: Int,
    val status: String,
    val createdAt: Long,
    val estimatedDeliveryDate: Long?
) 