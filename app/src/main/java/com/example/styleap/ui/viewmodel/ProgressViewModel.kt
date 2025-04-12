package com.example.styleap.ui.viewmodel

import com.example.styleap.domain.repository.ProgressRepository
import com.example.styleap.data.model.ProgressData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

data class ProgressUiState(
    val isLoading: Boolean = true,
    val progressData: ProgressData? = null,
    val actionInProgress: Boolean = false, // Specific loading for ad/withdraw
    val error: String? = null,
    val adRewardMessage: String? = null, // Event message
    val withdrawResultMessage: String? = null // Event message
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressRepository: ProgressRepository // Inject interface (non-nullable)
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgressData()
    }

    private fun loadProgressData() {
        viewModelScope.launch {
            progressRepository.getProgressData()
                .onStart {
                    Timber.d("Starting to collect progress data flow...")
                    // Set initial loading state if data is not yet loaded
                    if (_uiState.value.progressData == null) {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                }
                .catch { e ->
                    Timber.e(e, "Error collecting progress data flow")
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load progress: ${e.message}") }
                }
                .collect { data ->
                    Timber.d("Collected progress data: $data")
                    _uiState.update { it.copy(isLoading = false, progressData = data) }
                }
        }
    }

    fun watchAd() {
        val currentState = _uiState.value
        // Prevent action if another is in progress, or if ad isn't available (based on data)
        // Add more robust checks if needed (e.g., time-based availability)
        if (currentState.actionInProgress || currentState.progressData?.isAdAvailable != true) {
            Timber.w("Watch ad attempt blocked: actionInProgress=${currentState.actionInProgress}, isAdAvailable=${currentState.progressData?.isAdAvailable}")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgress = true, error = null, adRewardMessage = null) }

            val result = progressRepository.watchAdForPoints()

            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            actionInProgress = false,
                            adRewardMessage = "You earned $it points!"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { state ->
                        state.copy(
                            actionInProgress = false,
                            error = "Failed to get ad reward: ${e.message}"
                        )
                    }
                }
            )
        }
    }

    fun withdraw() {
        val currentState = _uiState.value
        // Prevent action if another is in progress, or if withdraw isn't enabled (based on data/rules)
        // The repository logic already checks the point threshold
        if (currentState.actionInProgress || currentState.progressData?.isWithdrawEnabled != true) {
            Timber.w("Withdraw attempt blocked: actionInProgress=${currentState.actionInProgress}, isWithdrawEnabled=${currentState.progressData?.isWithdrawEnabled}")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(actionInProgress = true, error = null, withdrawResultMessage = null) }

            val result = progressRepository.withdrawPoints()

            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            actionInProgress = false,
                            withdrawResultMessage = "Withdraw successful!"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { state ->
                        state.copy(
                            actionInProgress = false,
                            error = "Withdraw failed: ${e.message}"
                        )
                    }
                }
            )
        }
    }

    fun onRewardMessageConsumed() {
        _uiState.update { it.copy(adRewardMessage = null) }
    }
    fun onWithdrawMessageConsumed() {
        _uiState.update { it.copy(withdrawResultMessage = null) }
    }
    fun onErrorConsumed() {
        _uiState.update { it.copy(error = null) }
    }
} 