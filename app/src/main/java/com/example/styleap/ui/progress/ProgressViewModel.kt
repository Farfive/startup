package com.example.styleap.ui.progress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styleap.data.UserRepository
import com.example.styleap.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// Define UI state
sealed class ProgressUiState {
    object Initial : ProgressUiState()
    object Loading : ProgressUiState()
    object Success : ProgressUiState()
    data class Error(val message: String) : ProgressUiState()
}

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // LiveData for UI updates
    private val _userPoints = MutableLiveData(0)
    val userPoints: LiveData<Int> = _userPoints

    private val _userLevel = MutableLiveData(1)
    val userLevel: LiveData<Int> = _userLevel

    private val _progressValue = MutableLiveData(0)
    val progressValue: LiveData<Int> = _progressValue

    private val _withdrawalState = MutableLiveData<Resource<Unit>>()
    val withdrawalState: LiveData<Resource<Unit>> = _withdrawalState

    private val _isAdButtonEnabled = MutableLiveData(true)
    val isAdButtonEnabled: LiveData<Boolean> = _isAdButtonEnabled

    // UI State Flow
    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Initial)
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    // Minimum points required for withdrawal
    private val minWithdrawalPoints = 500

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = ProgressUiState.Loading
            try {
                when (val result = userRepository.getCurrentUserData()) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            _userPoints.value = user.points
                            _userLevel.value = user.level
                            _progressValue.value = calculateProgressValue(user.points)
                            _uiState.value = ProgressUiState.Success
                        } ?: run {
                            _uiState.value = ProgressUiState.Error("User data is null")
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = ProgressUiState.Error(result.message ?: "Unknown error")
                    }
                    is Resource.Loading -> {
                        // Already set to loading at the start
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading user data")
                _uiState.value = ProgressUiState.Error("Failed to load user data: ${e.message}")
            }
        }
    }

    fun incrementProgress(increment: Int) {
        val currentProgress = _progressValue.value ?: 0
        val newProgress = (currentProgress + increment).coerceIn(0, 100)
        _progressValue.value = newProgress
    }

    fun incrementPoints(increment: Int) {
        viewModelScope.launch {
            val currentPoints = _userPoints.value ?: 0
            val newPoints = currentPoints + increment
            
            // Update local UI immediately
            _userPoints.value = newPoints
            
            // Update backend
            when (val result = userRepository.incrementPoints(increment)) {
                is Resource.Success -> {
                    // Backend updated successfully
                    _userPoints.value = result.data?.points ?: newPoints
                }
                is Resource.Error -> {
                    // Revert to previous value if update fails
                    _userPoints.value = currentPoints
                    Timber.e("Failed to update points: ${result.message}")
                }
                is Resource.Loading -> {
                    // Already handled
                }
            }
            
            // Check if level should be updated
            checkAndUpdateLevel(newPoints)
        }
    }

    fun addPointsFromAd(points: Int) {
        viewModelScope.launch {
            val result = userRepository.addPointsForAd(points)
            when (result) {
                is Resource.Success -> {
                    result.data?.let { user ->
                        _userPoints.value = user.points
                        checkAndUpdateLevel(user.points)
                    }
                }
                is Resource.Error -> {
                    Timber.e("Failed to add points from ad: ${result.message}")
                }
                is Resource.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    private fun checkAndUpdateLevel(points: Int) {
        // Simple level calculation based on points
        val newLevel = (points / 500) + 1
        val currentLevel = _userLevel.value ?: 1
        
        if (newLevel > currentLevel) {
            _userLevel.value = newLevel
            viewModelScope.launch {
                userRepository.updateUserLevel(newLevel)
            }
        }
    }

    private fun calculateProgressValue(points: Int): Int {
        // Calculate progress % based on points to next level
        val currentLevel = _userLevel.value ?: 1
        val pointsForCurrentLevel = (currentLevel - 1) * 500
        val pointsToNextLevel = currentLevel * 500
        val progress = ((points - pointsForCurrentLevel).toFloat() / (pointsToNextLevel - pointsForCurrentLevel)) * 100
        return progress.toInt().coerceIn(0, 100)
    }

    fun attemptWithdrawal() {
        viewModelScope.launch {
            val currentPoints = _userPoints.value ?: 0
            if (currentPoints < minWithdrawalPoints) {
                return@launch
            }
            
            _withdrawalState.value = Resource.Loading()
            
            // Example withdrawal details - in a real app, would be collected from UI
            val withdrawalDetails = mapOf(
                "method" to "bank_transfer",
                "accountNumber" to "XXXX-XXXX-XXXX-1234"
            )
            
            val result = userRepository.withdrawPoints(minWithdrawalPoints, withdrawalDetails)
            when (result) {
                is Resource.Success -> {
                    // Update local points
                    _userPoints.value = currentPoints - minWithdrawalPoints
                    _withdrawalState.value = Resource.Success(Unit)
                }
                is Resource.Error -> {
                    _withdrawalState.value = Resource.Error(result.message ?: "Unknown error")
                }
                is Resource.Loading -> {
                    // Already set to loading
                }
            }
        }
    }

    fun resetWithdrawalState() {
        _withdrawalState.value = null
    }

    fun canWithdraw(): Boolean {
        return (_userPoints.value ?: 0) >= minWithdrawalPoints
    }
} 