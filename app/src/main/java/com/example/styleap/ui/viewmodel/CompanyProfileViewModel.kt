package com.example.styleap.ui.viewmodel

import com.example.styleap.domain.repository.CompanyRepository
import com.example.styleap.data.model.Company
import com.example.styleap.data.model.EmployeeInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

data class CompanyProfileUiState(
    val isLoading: Boolean = true,
    val company: Company? = null,
    val employees: List<EmployeeInfo> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class CompanyProfileViewModel @Inject constructor(
    private val companyRepository: CompanyRepository // Inject interface (non-nullable)
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyProfileUiState())
    val uiState: StateFlow<CompanyProfileUiState> = _uiState.asStateFlow()

    init {
        loadCompanyData()
    }

    private fun loadCompanyData() {
        // Combine the flows for company details and employees
        viewModelScope.launch {
            try {
                // Combine flows or load sequentially
                companyRepository.getCompanyDetails()
                    .combine(companyRepository.getCompanyEmployees()) { details, employees ->
                        Timber.d("Combining company details ($details) and employees (${employees.size})")
                        // Create state with combined data, reset loading and error
                        CompanyProfileUiState(isLoading = false, company = details, employees = employees, error = null)
                    }
                    .onStart {
                         Timber.d("Starting to collect combined company/employee flow...")
                         // Set initial loading state
                         _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    .catch { e ->
                        Timber.e(e, "Error combining company details and employees flow")
                        // Emit error state
                        _uiState.update { it.copy(isLoading = false, error = "Error loading company data: ${e.message}") }
                    }
                    .collect { combinedState ->
                        Timber.d("Collected combined state: $combinedState")
                        _uiState.value = combinedState // Update the UI state
                    }
            } catch (e: Exception) {
                // Catch potential errors during flow creation itself (less likely)
                Timber.e(e, "Failed to initiate company data loading")
                _uiState.update { it.copy(isLoading = false, error = "Failed to load company data: ${e.message}") }
            }
        }
    }

    // Placeholder functions for button actions
    fun addPhoto() {
        viewModelScope.launch { /* TODO: Implement add photo logic */ }
    }

    fun setHours() {
        viewModelScope.launch { /* TODO: Implement set hours logic */ }
    }

    fun addPriceList() {
        viewModelScope.launch { /* TODO: Implement add price list logic */ }
    }

    fun onEmployeeSelected(employee: EmployeeInfo) {
        Timber.d("Employee selected: $employee")
        // TODO: Handle navigation to employee detail or other action
    }

    fun onErrorConsumed() {
        _uiState.update { it.copy(error = null) }
    }
} 