package com.cyberqbit.ceptekabin.ui.screens.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberqbit.ceptekabin.data.remote.firebase.FirestoreService
import com.cyberqbit.ceptekabin.data.remote.firebase.GoogleAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val googleAuthService: GoogleAuthService,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _signInState = MutableStateFlow<SignInState>(SignInState.Idle)
    val signInState: StateFlow<SignInState> = _signInState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(googleAuthService.isUserSignedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun getGoogleSignInIntent(): Intent {
        return googleAuthService.signInIntent
    }

    fun handleGoogleSignInResult(data: Intent?) {
        if (data == null) {
            _errorMessage.value = "Sign-in canceled"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = googleAuthService.signInWithGoogle(data)) {
                is GoogleAuthService.SignInResult.Success -> {
                    // Save user to Firestore asynchronously without blocking login flow
                    val userData = mapOf(
                        "uid" to result.uid,
                        "email" to result.email,
                        "displayName" to result.displayName,
                        "photoUrl" to (result.photoUrl ?: ""),
                        "createdAt" to System.currentTimeMillis()
                    )
                    viewModelScope.launch {
                        firestoreService.saveUser(result.uid, userData)
                    }
                    _isLoggedIn.value = true
                    _signInState.value = SignInState.Success(result.uid)
                }
                is GoogleAuthService.SignInResult.Error -> {
                    _errorMessage.value = result.message
                    _signInState.value = SignInState.Error(result.message)
                }
            }

            _isLoading.value = false
        }
    }

    fun signOut() {
        googleAuthService.signOut()
        _isLoggedIn.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    sealed class SignInState {
        object Idle : SignInState()
        data class Success(val userId: String) : SignInState()
        data class Error(val message: String) : SignInState()
    }
}
