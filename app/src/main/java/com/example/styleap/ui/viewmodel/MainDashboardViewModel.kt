package com.example.styleap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styleap.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // Navigation should be handled in the Composable based on auth state changes
            // or by observing an auth state flow.
        }
    }
} 