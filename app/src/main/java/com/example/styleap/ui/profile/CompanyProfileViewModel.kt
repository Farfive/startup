package com.example.styleap.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styleap.data.CompanyRepository
import com.example.styleap.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CompanyProfileViewModel @Inject constructor(
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _companyName = MutableLiveData<String>()
    val companyName: LiveData<String> = _companyName

    private val _companyType = MutableLiveData<String>()
    val companyType: LiveData<String> = _companyType

    private val _employees = MutableLiveData<List<String>>()
    val employees: LiveData<List<String>> = _employees

    private val _points = MutableLiveData<Int>()
    val points: LiveData<Int> = _points

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadCompanyData()
    }

    fun loadCompanyData() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                val result = companyRepository.getCurrentCompany()
                
                when (result) {
                    is Resource.Success -> {
                        result.data?.let { company ->
                            _companyName.value = company.name
                            _companyType.value = company.companyType
                            _employees.value = company.employees
                            _points.value = company.points
                        }
                    }
                    is Resource.Error -> {
                        _error.value = result.message ?: "Unknown error occurred"
                        Timber.e("Error loading company data: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Already handling loading state
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load company data: ${e.message}"
                Timber.e(e, "Exception when loading company data")
            } finally {
                _loading.value = false
            }
        }
    }

    fun setCompanyName(name: String) {
        _companyName.value = name
    }

    fun setCompanyType(type: String) {
        _companyType.value = type
    }

    fun setEmployees(employees: List<String>) {
        _employees.value = employees
    }

    fun setPoints(points: Int) {
        _points.value = points
    }

    fun updateCompanyInfo() {
        viewModelScope.launch {
            _loading.value = true
            
            try {
                // Gather current values
                val currentName = _companyName.value ?: return@launch
                val currentType = _companyType.value ?: return@launch
                val currentPoints = _points.value ?: 0
                
                // Call repository to update company info
                val result = companyRepository.updateCompanyInfo(
                    name = currentName,
                    companyType = currentType,
                    points = currentPoints
                )
                
                when (result) {
                    is Resource.Success -> {
                        // Refresh company data after successful update
                        loadCompanyData()
                    }
                    is Resource.Error -> {
                        _error.value = result.message ?: "Failed to update company info"
                    }
                    is Resource.Loading -> {
                        // Already handling loading state
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error updating company info: ${e.message}"
                Timber.e(e, "Exception when updating company info")
            } finally {
                _loading.value = false
            }
        }
    }
} 