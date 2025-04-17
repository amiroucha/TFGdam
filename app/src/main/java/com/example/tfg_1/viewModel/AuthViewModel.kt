package com.example.tfg_1.viewModel

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class AuthViewModel : ViewModel(){

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState= MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthStatus()
    }
/* para el login  --------------------------------------------*/
    fun onLoginChanges(email: String, password: String) {
        _email.value = email
        _password.value = password
    }


/* para el login  --------------------------------------------*/

    fun checkAuthStatus(){
        if(auth.currentUser!=null){
            _authState.value = AuthState.Authenticated
        }else {
            _authState.value = AuthState.Unauthenticated
        }

    }

    fun login(email: String, password: String){
        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }
                else {
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }
    fun signUp(email: String, password: String, password2: String){

        if(email.isEmpty() || password.isEmpty() || password2.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        if (password != password2){
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }
                else {
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }

    }
    fun signOut(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

}


sealed class AuthState{
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val error: String) : AuthState()
}