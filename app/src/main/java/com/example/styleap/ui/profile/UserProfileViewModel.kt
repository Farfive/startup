package com.example.styleap.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styleap.data.model.User
import com.example.styleap.domain.repository.UserRepository
import com.example.styleap.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userData = MutableLiveData<Resource<User>>()
    val userData: LiveData<Resource<User>> = _userData

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _userData.value = Resource.Loading()
            try {
                val user = userRepository.getUserData()
                if (user != null) {
                    _userData.value = Resource.Success(user)
                } else {
                    _userData.value = Resource.Error("User data not found.")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load user data")
                _userData.value = Resource.Error(e.message ?: "Failed to load user data")
            }
        }
    }

    // Removed setUsername and setPoints as they used placeholder methods.
    // Updates would typically be handled via specific use cases or a generic update function.

    // Example of how you might trigger premium purchase (if needed in this ViewModel)
    /*
    private val _purchaseStatus = MutableLiveData<Resource<Unit>>()
    val purchaseStatus: LiveData<Resource<Unit>> = _purchaseStatus

    fun purchasePremiumFeature() {
        viewModelScope.launch {
            _purchaseStatus.value = Resource.Loading()
            val result = userRepository.purchasePremium()
            if (result.isSuccess) {
                _purchaseStatus.value = Resource.Success(Unit)
                loadUserData() // Refresh user data to show premium status
            } else {
                _purchaseStatus.value = Resource.Error(result.exceptionOrNull()?.message ?: "Purchase failed")
            }
        }
    }
    */
} 