package com.example.dream.ui.season // Adjust package name if needed (e.g., com.example.styleap)

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styleap.data.model.Season
import com.example.styleap.data.model.UserSeasonProgress
import com.example.dream.data.repository.SeasonRepository
import com.example.styleap.domain.repository.UserRepository
import com.example.dream.domain.service.SeasonService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

private const val TAG = "SeasonViewModel"

@HiltViewModel
class SeasonViewModel @Inject constructor(
    private val seasonService: SeasonService,
    private val seasonRepository: SeasonRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _activeSeason = MutableStateFlow<Season?>(null)
    val activeSeason: StateFlow<Season?> = _activeSeason.asStateFlow()

    private val _userSeasonProgress = MutableStateFlow<UserSeasonProgress?>(null)
    val userSeasonProgress: StateFlow<UserSeasonProgress?> = _userSeasonProgress.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadInitialSeasonData()
    }

    /**
      * Loads the current active season and the user's progress for it.
      * Assumes the reset check has likely already run during app startup.
      */
    private fun loadInitialSeasonData() {
        _isLoading.value = true
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            if (userId == null) {
                 Log.w(TAG, "User not logged in, cannot load season data.")
                _isLoading.value = false
                // Potentially set state to indicate user needs to login
                return@launch
            }

            // Determine active season
            val currentSeason = seasonService.getCurrentActiveSeason()
            _activeSeason.value = currentSeason
            Log.d(TAG, "Loaded active season: ${currentSeason?.seasonId ?: "None"}")


            if (currentSeason != null) {
                // Fetch progress specifically for the determined active season
                // Fetch directly from repository to get latest state after potential reset
                val progress = seasonRepository.getUserSeasonProgress(userId, currentSeason.seasonId)
                _userSeasonProgress.value = progress
                 Log.d(TAG, "Loaded user progress for season ${currentSeason.seasonId}: ${progress?.level ?: "Not Found"}")
            } else {
                // No active season, clear progress display
                _userSeasonProgress.value = null
            }
            _isLoading.value = false
        }
    }

    /**
     * Call this function if you need to refresh the season data displayed in the UI,
     * for example, after the user earns points.
     */
    fun refreshSeasonData() {
         Log.d(TAG, "Refreshing season data...")
        loadInitialSeasonData()
    }
} 