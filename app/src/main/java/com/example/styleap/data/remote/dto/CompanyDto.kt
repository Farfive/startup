package com.example.styleap.data.remote.dto

data class CompanyDto(
    val id: String,
    val name: String,
    val companyType: String,
    val points: Int,
    val employees: List<String> = emptyList(),
    val isPremium: Boolean = false,
    val premiumExpiryDate: Long? = null
) 