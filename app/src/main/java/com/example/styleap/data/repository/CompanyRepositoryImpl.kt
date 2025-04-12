package com.example.styleap.data.repository

import com.example.styleap.data.Company
import com.example.styleap.data.CompanyRepository
import com.example.styleap.data.remote.PointsApiService
import com.example.styleap.data.remote.dto.CompanyDto // Assuming DTO and mapping
import com.example.styleap.util.Resource
import timber.log.Timber
import javax.inject.Inject
import retrofit2.HttpException
import java.io.IOException

// TODO: Implement mapper between Company (domain) and CompanyDto (network)
// fun CompanyDto.toCompany(): Company { ... }

class CompanyRepositoryImpl @Inject constructor(
    private val apiService: PointsApiService // Inject API service
) : CompanyRepository {

    // Reusable safeApiCall helper (could be moved to a common utility)
    private suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): Resource<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("API returned empty body")
            } else {
                Timber.e("API Error: Code=${response.code()}, Message=${response.message()}, ErrorBody=${response.errorBody()?.string()}")
                Resource.Error("API Error ${response.code()}: ${response.message()}")
            }
        } catch (e: HttpException) {
            Timber.e(e, "Network error (HTTP)")
            Resource.Error("Network error: ${e.message()}")
        } catch (e: IOException) {
            Timber.e(e, "Network error (IO)")
            Resource.Error("Network connection error.")
        } catch (e: Exception) {
            Timber.e(e, "Unknown error during API call")
            Resource.Error("An unexpected error occurred: ${e.message}")
        }
    }

    override suspend fun getCompany(id: String): Resource<Company> {
         // Assuming CompanyDto can be mapped or is compatible with Company
         return when (val result = safeApiCall { apiService.getCompany(id) }) {
            is Resource.Success -> Resource.Success(result.data!!) // Replace with mapping: result.data!!.toCompany()
            is Resource.Error -> Resource.Error(result.message ?: "Failed to get company data")
            is Resource.Loading -> Resource.Loading() // Should not happen here
        }
    }

    // Implement other methods (createCompany, updateCompany, etc.) if defined in the interface
    // using similar safeApiCall patterns and appropriate apiService methods.
} 