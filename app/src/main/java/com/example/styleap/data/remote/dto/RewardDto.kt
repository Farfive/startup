package com.example.styleap.data.remote.dto

data class RewardDto(
    val id: String,
    val name: String,
    val description: String,
    val pointsRequired: Int,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true
) 