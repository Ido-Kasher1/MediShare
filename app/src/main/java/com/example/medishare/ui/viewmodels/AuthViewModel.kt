package com.example.medishare.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medishare.data.AuthRepository
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeoutException

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _authState.value = if (repository.isUserLoggedIn()) {
            AuthState.Authenticated(repository.currentUser?.email ?: "")
        } else {
            AuthState.Unauthenticated
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = repository.signIn(email, password)
                result.onSuccess { user ->
                    if (user != null) {
                        _authState.value = AuthState.Authenticated(email)
                    } else {
                        _authState.value = AuthState.Error("Authentication failed")
                    }
                }.onFailure { e ->
                    val errorMessage = when (e) {
                        is TimeoutException -> "Network timeout. Please check your internet connection and try again."
                        else -> e.message ?: "Sign in failed"
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = repository.signUp(email, password)
                result.onSuccess { user ->
                    if (user != null) {
                        _authState.value = AuthState.Authenticated(email)
                    } else {
                        _authState.value = AuthState.Error("Registration failed")
                    }
                }.onFailure { e ->
                    val errorMessage = when (e) {
                        is TimeoutException -> "Network timeout. Please check your internet connection and try again."
                        else -> e.message ?: "Sign up failed"
                    }
                    _authState.value = AuthState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
