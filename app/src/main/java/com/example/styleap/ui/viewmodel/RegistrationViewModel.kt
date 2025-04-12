package com.example.styleap.ui.viewmodel

import com.example.styleap.domain.repository.AuthRepository
import com.example.styleap.data.model.UserType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistrationUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val registrationSuccessEvent: Boolean = false // One-time event for navigation
)

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository // Now using the real repository via Hilt
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String, userType: UserType) {
        if (_uiState.value.isLoading) return

        // Basic validation
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your name.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
             _uiState.update { it.copy(error = "Please enter a valid email address.") }
             return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters long.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, registrationSuccessEvent = false) }

            val result = authRepository.register(name, email, password, userType) // Call the real repository

            result.fold(
                onSuccess = {
                     // Registration successful
                    _uiState.update { it.copy(isLoading = false, registrationSuccessEvent = true) }
                },
                onFailure = { exception ->
                     // Registration failed, use message from mapped exception
                    _uiState.update { it.copy(isLoading = false, error = exception.message ?: "Registration failed.") }
                }
            )
        }
    }

    fun onRegistrationSuccessEventConsumed() {
        _uiState.update { it.copy(registrationSuccessEvent = false) }
    }

    fun onErrorConsumed() {
        _uiState.update { it.copy(error = null) }
    }
} 