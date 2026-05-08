package com.receiptvault.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.receiptvault.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repository: ReceiptRepository
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun signInWithEmail(email: String, password: String) {
        if (!validateEmailPassword(email, password)) return
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                result.user?.let { user ->
                    _authState.value = AuthState.Success(user)
                    syncAfterLogin()
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Sign-in failed")
            }
    }

    fun signUpWithEmail(email: String, password: String) {
        if (!validateEmailPassword(email, password)) return
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                result.user?.let { user ->
                    _authState.value = AuthState.Success(user)
                    syncAfterLogin()
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Sign-up failed")
            }
    }

    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                result.user?.let { user ->
                    _authState.value = AuthState.Success(user)
                    syncAfterLogin()
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Google sign-in failed")
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    private fun syncAfterLogin() {
        viewModelScope.launch {
            runCatching { repository.syncAllToCloud() }
        }
    }

    private fun validateEmailPassword(email: String, password: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Please enter a valid email address")
            return false
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return false
        }
        return true
    }

    fun isLoggedIn() = auth.currentUser != null
    fun getCurrentUser() = auth.currentUser
    fun resetState() { _authState.value = AuthState.Idle }
}
