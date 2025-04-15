package com.example.tfg_1.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel(){

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState= MutableLiveData<AuthState>()
    private val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus(){
        if(auth.currentUser!=null){
            _authState.value = AuthState.Authenticated
        }else
        {
            _authState.value = AuthState.Unauthenticated
        }

    }

    fun login(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }
                else {
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }


    }
}


sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val error: String) : AuthState()
}