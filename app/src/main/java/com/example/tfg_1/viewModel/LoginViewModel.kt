package com.example.tfg_1.viewModel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay

class LoginViewModel : ViewModel() {

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> get() = _password

    private val _loginEnable = MutableLiveData<Boolean>()
    val loginEnable: LiveData<Boolean> = _loginEnable

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    //validar la contraseña

    private fun ValidPassword(password: String): Boolean = password.length > 8
    //validar el correo
    private fun ValidEmail(email: String): Boolean  = Patterns.EMAIL_ADDRESS.matcher(email).matches()


    fun onLoginChange(email: String, password: String) {
        _email.value = email
        _password.value = password
        _loginEnable.value = ValidEmail(email) && ValidPassword(password)
    }

    suspend fun onLoginSelected() { //para el carga
        _isLoading.value = true
        delay(4000)
        _isLoading.value = false
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
}
