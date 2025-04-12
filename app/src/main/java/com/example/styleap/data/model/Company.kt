package com.example.styleap.data.model

// Placeholder Data Class (ideally move to data/domain layer)
data class Company(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val points: Int = 0,
    val logoUrl: String? = null // For AsyncImage if used
) 