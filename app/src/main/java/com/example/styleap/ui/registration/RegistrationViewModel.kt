package com.example.styleap.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styleap.data.AuthRepository
import com.example.styleap.data.RegistrationParams
import com.example.styleap.data.RegistrationResult
import com.example.styleap.data.UserType
import com.example.styleap.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // LiveData for registration state
    private val _registrationState = MutableLiveData<Resource<RegistrationResult>>()
    val registrationState: LiveData<Resource<RegistrationResult>> = _registrationState

    fun attemptRegistration(params: RegistrationParams) {
        viewModelScope.launch {
            _registrationState.value = Resource.Loading() // Notify UI about loading state
            val result = authRepository.registerUser(params)
            _registrationState.value = result // Post the final result (Success or Error)
        }
    }
} 