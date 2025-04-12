package com.example.styleap.data

data class Company(
    val id: String,
    val name: String,
    val description: String? = null,
    val address: String? = null
) 