package com.example.styleap.data.remote.dto

data class UserDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val userType: String = "Individual",
    val points: Int = 0,
    val level: Int = 1,
    val isPremium: Boolean = false,
    val premiumExpiryDate: Long? = null
    // Add other fields that match your backend API
) 