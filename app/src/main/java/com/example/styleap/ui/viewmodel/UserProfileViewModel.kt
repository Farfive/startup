package com.example.styleap.ui.viewmodel

import com.example.styleap.domain.repository.UserRepository
import com.example.styleap.data.model.User
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
    val premiumPurchaseResult: Boolean? = null // Event for purchase success/failure
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository // Inject interface (non-nullable)
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.getUserData()
                .onStart {
                    Timber.d("Starting to collect user data flow...")
                    // Emit loading state only if user is initially null
                    if (_uiState.value.user == null) {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                }
                .catch { e ->
                    Timber.e(e, "Error collecting user data flow")
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load user data: ${e.message}") }
                }
                .collect { user ->
                    Timber.d("Collected user data: $user")
                    _uiState.update { it.copy(isLoading = false, user = user) }
                }
        }
    }

    fun purchasePremium() {
        val currentState = _uiState.value
        // Prevent purchase if already loading, user not loaded, or already premium
        if (currentState.isLoading || currentState.user == null || currentState.user.premiumStatus) {
            Timber.w("Purchase premium attempt blocked: isLoading=${currentState.isLoading}, userNull=${currentState.user == null}, isPremium=${currentState.user?.premiumStatus}")
            return
        }

        viewModelScope.launch {
            // Use a specific loading state for the purchase action if needed, or reuse isLoading
            _uiState.update { it.copy(isLoading = true, error = null, premiumPurchaseResult = null) } // Indicate loading for purchase

            val result = userRepository.purchasePremium()

            result.fold(
                onSuccess = { success ->
                    Timber.i("Premium purchase successful.")
                    // Flow will automatically update user data, just update the event state
                    // isLoading will be reset by the flow emitting new user data
                    _uiState.update { it.copy(premiumPurchaseResult = success, isLoading = false) }
                },
                onFailure = { exception ->
                    Timber.e(exception, "Premium purchase failed.")
                    _uiState.update { it.copy(isLoading = false, premiumPurchaseResult = false, error = exception.message ?: "Premium purchase failed.") }
                }
            )
        }
    }

    fun onPremiumPurchaseResultConsumed() {
        _uiState.update { it.copy(premiumPurchaseResult = null) }
    }

    fun onErrorConsumed() {
        _uiState.update { it.copy(error = null) }
    }
} 