package com.example.tfg_1.viewModel

sealed class AuthStateClass{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val error: String) : AuthState()
}