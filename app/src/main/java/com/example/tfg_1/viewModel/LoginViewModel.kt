package com.example.tfg_1.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import android.util.Patterns

class LoginViewModel : ViewModel() {

    // Usamos StateFlow en lugar de LiveData
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _loginEnable = MutableStateFlow(false)
    val loginEnable: StateFlow<Boolean> = _loginEnable.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    //validar la contraseña
    private fun validPassword(password: String): Boolean = password.length > 8

    //validar el correo
    private fun validEmail(email: String): Boolean  = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun onLoginChanges(email: String, password: String) {
        _email.value = email
        _password.value = password
        _loginEnable.value = validEmail(email) && validPassword(password)
    }

    suspend fun onLoginSelected() { //para el carga
        _isLoading.value = true
        delay(4000)
        _isLoading.value = false
    }

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
