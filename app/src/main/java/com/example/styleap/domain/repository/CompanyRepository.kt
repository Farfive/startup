package com.example.styleap.domain.repository

import com.example.styleap.data.model.Company
import com.example.styleap.data.model.EmployeeInfo
import kotlinx.coroutines.flow.Flow

// Placeholder Repository
interface CompanyRepository {
    suspend fun getCompanyDetails(): Flow<Company?> // Maybe by ID? getCompanyDetails(companyId: String)
    suspend fun getCompanyEmployees(): Flow<List<EmployeeInfo>> // Maybe by ID? getCompanyEmployees(companyId: String)
    // Add functions for addPhoto, setHours, addPriceList
} 