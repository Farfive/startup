package com.example.styleap.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.styleap.data.AuthRepository
import com.example.styleap.data.LoginParams
import com.example.styleap.data.LoginResult
import com.example.styleap.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<LoginResult>>()
    val loginState: LiveData<Resource<LoginResult>> = _loginState

    fun attemptLogin(params: LoginParams) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            val result = authRepository.loginUser(params)
            _loginState.value = result
        }
    }
} 