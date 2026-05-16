package com.afelix.rifaapp.ui.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Guest : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(
        if (auth.currentUser != null) AuthState.Authenticated else AuthState.Initial
    )
    val authState: StateFlow<AuthState> = _authState

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)
                
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("335547182069-gsof48ime75h5uon4mpovhaqo7d50n44.apps.googleusercontent.com")
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error(e.message ?: "Error al obtener credenciales")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is GoogleIdTokenCredential) {
            val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
            try {
                auth.signInWithCredential(firebaseCredential)
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Fallo en Firebase: ${e.message}")
            }
        } else {
            _authState.value = AuthState.Error("Tipo de credencial no soportado")
        }
    }

    fun continueAsGuest() {
        _authState.value = AuthState.Guest
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Initial
    }
}
