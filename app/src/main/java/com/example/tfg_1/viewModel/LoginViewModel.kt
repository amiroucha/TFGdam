package com.example.tfg_1.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import android.util.Patterns
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.tfg_1.navigation.Screens
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel(navController: NavController) : ViewModel() {
    private val _navController = navController

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> get() = _emailError

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> get() = _passwordError

    private val _loginEnable = MutableStateFlow(false)
    val loginEnable: StateFlow<Boolean> = _loginEnable.asStateFlow()

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
        _loginEnable.value = validEmail(email) && validPassword(password)
    }

    suspend fun onLoginSelected() {
        if (validateOnSubmit()) {
            _isLoading.value = true
            delay(2000)
            _isLoading.value = false
        }
    }

    fun validateOnSubmit(): Boolean {
        val email = _email.value
        val password = _password.value

        _emailError.value = when {
            email.isEmpty() -> "El correo no puede estar vacío"
            !validEmail(email) -> "Correo incorrecto"
            else -> null
        }

        _passwordError.value = when {
            password.isEmpty() -> "La contraseña no puede estar vacía"
            !validPassword(password) -> "La contraseña debe tener más longitud"
            else -> null
        }

        return validEmail(email) && validPassword(password)
    }

    private fun checkAuthStatus() {
        _authState.value = if (auth.currentUser != null) AuthState.Authenticated else AuthState.Unauthenticated
    }

    fun login(email: String, password: String) {
        if (validateOnSubmit()) {
            _authState.value = AuthState.Loading
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Authenticated
                        _navController.navigate(Screens.Home.route)
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "ERROR. Algo fue mal")
                    }
                }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val error: String) : AuthState()
}

   /* private val _emailError = MutableLiveData<String?>() //para almacenar los errores
    val emailError: LiveData<String?> get() = _emailError

    private val _passwordError = MutableLiveData<String?>() //para almacenar los errores
    val passwordError: LiveData<String?> get() = _passwordError

    private val _isFormValid = MutableLiveData<Boolean>()
    val isFormValid: LiveData<Boolean> get() = _isFormValid


    fun setEmail(email: String) {
        _email.value = email
        if (email.isNotEmpty()) _emailError.value = null
        //cuando ya no esta vaciose quita el mensaje de errir
    }

    fun setPassword(password: String) {
        _password.value = password
        if (password.isNotEmpty()) _passwordError.value = null
    //cuando ya no esta vaciose quita el mensaje de errir
    }

    fun validateOnSubmit(): Boolean {
        val email = _email.value.orEmpty()
        val password = _password.value.orEmpty()

        _emailError.value = if (email.isEmpty()) "El correo no puede estar vacío" else null
        _passwordError.value = if (password.isEmpty()) "La contraseña no puede estar vacía" else null

        val isValid = email.isNotEmpty() && password.isNotEmpty()
        _isFormValid.value = isValid
        return isValid
    }*/
