package com.isankamil.mcjobid.domain.model

/**
 * Explicit state model for Authentication Lifecycle.
 * Prevents contradictory states like isLoggedIn=true + isLoading=true + user=null.
 */
sealed class AuthUiState {
    object Loading : AuthUiState()
    
    data class Authenticated(
        val uid: String,
        val email: String?
    ) : AuthUiState()

    object Unauthenticated : AuthUiState()

    data class Error(val message: String) : AuthUiState()
}
