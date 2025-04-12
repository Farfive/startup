package com.example.styleap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Import destinations if needed for navigation events, though handled in NavHost usually
// import com.example.styleap.ui.navigation.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.styleap.domain.repository.AuthRepository
import com.example.styleap.data.model.UserType

// Example placeholder for Auth Repository (can be moved to data layer)

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccessEvent: Boolean = false // One-time event to trigger navigation
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository // Now using the real repository via Hilt
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (_uiState.value.isLoading) return // Prevent multiple clicks

        // Basic validation
        if (email.isBlank() || password.isBlank()) {
             _uiState.update { it.copy(error = "Please enter both email and password.") }
             return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, loginSuccessEvent = false) }

            val result = authRepository.login(email, password) // Call the real repository

            result.fold(
                onSuccess = {
                    // Login successful
                    _uiState.update { it.copy(isLoading = false, loginSuccessEvent = true) }
                },
                onFailure = { exception ->
                    // Login failed, use the message from the mapped exception
                    _uiState.update { it.copy(isLoading = false, error = exception.message ?: "Login failed.") }
                }
            )
        }
    }

    // Call this from the Composable after navigation has occurred
    fun onLoginSuccessEventConsumed() {
        _uiState.update { it.copy(loginSuccessEvent = false) }
    }

    // Call this if the error message is handled (e.g., shown in a Snackbar/Toast)
    fun onErrorConsumed() {
        _uiState.update { it.copy(error = null) }
    }
} 