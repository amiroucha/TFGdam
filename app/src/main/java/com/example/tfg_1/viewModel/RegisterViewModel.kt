package com.example.tfg_1.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> get() = _password.asStateFlow()

    private val _date = MutableStateFlow("")
    val date: StateFlow<String> get() = _date.asStateFlow()

    private val _emailError = MutableStateFlow("")
    val emailError: StateFlow<String?> get() = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow("")
    val passwordError: StateFlow<String?> get() = _passwordError.asStateFlow()


    private val _dateError = MutableStateFlow("")
    val dateError: StateFlow<String?> get() = _dateError.asStateFlow()

    private val _isFormValid = MutableStateFlow<Boolean>(false)


    fun setEmail(email: String) {
        _email.value = email
        if (email.isNotEmpty()) _emailError.value = null.toString()
    }

    fun setPassword(password: String) {
        _password.value = password
        if (password.isNotEmpty()) _passwordError.value = null.toString()
    }

    fun setDate(date: String) {
        _date.value = date

        if (date.isNotEmpty() && date != "d-M-yyyy") {
            _dateError.value = null.toString()
        }
    }

    fun validateOnSubmit(): Boolean {
        val email = _email.value.orEmpty()
        val password = _password.value.orEmpty()
        val date = _date.value.orEmpty()


        _emailError.value = if (email.isEmpty()) "El correo no puede estar vacío" else null.toString()
        _passwordError.value = if (password.isEmpty()) "La contraseña no puede estar vacía" else null.toString()
        _dateError.value = if (date.isEmpty() || date == "d-M-yyyy") "Debes seleccionar una fecha" else null.toString()


        val isValid = email.isNotEmpty() && password.isNotEmpty() && date.isNotEmpty() && date != "d-M-yyyy"
        _isFormValid.value = isValid
        return isValid
    }
}
