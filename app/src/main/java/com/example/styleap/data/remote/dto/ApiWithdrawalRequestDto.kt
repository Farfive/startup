package com.example.styleap.data.remote.dto

data class ApiWithdrawalRequestDto(
    val userId: String,
    val points: Int,
    val details: Map<String, String>
) 