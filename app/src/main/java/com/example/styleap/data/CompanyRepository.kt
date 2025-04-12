package com.example.styleap.data

import com.example.styleap.util.Resource

// Interface for Company related data operations
interface CompanyRepository {
    // Fetch company details by ID
    suspend fun getCompany(id: String): Resource<Company> // Return Resource<Company>

    // Optional: Add other methods if needed by the application
    // suspend fun createCompany(company: Company): Resource<Company>
    // suspend fun updateCompany(company: Company): Resource<Company>
    // suspend fun getCompanyEmployees(companyId: String): Resource<List<User>>
} 