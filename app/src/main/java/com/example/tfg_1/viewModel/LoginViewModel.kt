package com.example.tfg_1.viewModel

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.tfg_1.R
import com.example.tfg_1.navigation.Screens
import com.example.tfg_1.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


class LoginViewModel : ViewModel()
{
    private val userRepository = UserRepository()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _passwordResetMessage = MutableStateFlow<String?>(null)
    val passwordResetMessage: StateFlow<String?> = _passwordResetMessage

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    private fun validPassword(password: String): Boolean = password.length >= 8

    private fun validEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotEmpty()

    fun onLoginChanges(email: String, password: String) {
        _email.value = email
        _password.value = password
       // _loginEnable.value = validEmail(email) && validPassword(password)

        // Limpiar errores al modificar campos
        _emailError.value = null
        _passwordError.value = null
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }


    private fun validateOnSubmit(context: Context): Boolean {
        val email = _email.value
        val password = _password.value

        _emailError.value = when {
            email.isEmpty() -> context.getString(R.string.correoNoPuedeVacio)
            !validEmail(email) -> context.getString(R.string.correo_incorrecto)
            else -> null
        }

        _passwordError.value = when {
            password.isEmpty() -> context.getString(R.string.la_contrase_a_no_puede_estar_vac_a)
            !validPassword(password) -> context.getString(R.string.la_contrase_a_debe_tener_m_s_longitud)
            else -> null
        }

        return _emailError.value == null && _passwordError.value == null
    }

    private fun checkAuthStatus() {
        _authState.value = if (auth.currentUser != null) AuthState.Authenticated else AuthState.Unauthenticated
    }

    fun sendResetPassword(email: String, context: Context) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _passwordResetMessage.value =context.getString(R.string.correoReestablecerContra)
                } else {
                    _passwordResetMessage.value =
                        context.getString(R.string.no_se_pudo_enviar_el_correo_verifica_el_email)
                }
            }
    }
    fun clearPasswordResetMessage() {
        _passwordResetMessage.value = null
    }
    fun setPasswordResetError(message: String) {
        _passwordResetMessage.value = message
    }

    fun login(navController: NavController, context: Context) {
        val email = _email.value
        val password = _password.value
        if (!validateOnSubmit(context)) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        _isLoading.value = true
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = userRepository.loginWithEmail(email, password)
            _isLoading.value = false

            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
                //val user = result.getOrNull()
                val homeId = userRepository.getCurrentUserHomeId()

                if (homeId != "") {
                    navController.navigate(Screens.Tasks.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                }
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: context.getString(R.string.error_algo_fue_mal))
            }
        }
    }



    fun loginGoogle(context: Context, navController: NavController) {
        _isLoading.value = true
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = userRepository.loginWithGoogle(context)
            _isLoading.value = false

            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
                //val user = result.getOrNull()
                val homeId = userRepository.getCurrentUserHomeId()

                if (homeId != "") {
                    navController.navigate(Screens.Tasks.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                }
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: context.getString(R.string.error_login_google))
            }
        }
    }
    fun logout() {
        userRepository.logout() // cierra sesión en Firebase
        _email.value = ""
        _password.value = ""
        _authState.value = AuthState.Unauthenticated
    }


    sealed class AuthState {
        data object Authenticated : AuthState()
        data object Unauthenticated : AuthState()
        data object Loading : AuthState()
        data class Error(val error: String) : AuthState()
    }
}



