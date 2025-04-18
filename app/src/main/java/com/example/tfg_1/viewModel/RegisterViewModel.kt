package com.example.tfg_1.viewModel
import android.util.Patterns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController

class RegisterViewModel(navController: NavController) : ViewModel() {
    private val _navController = navController

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _password2 = MutableStateFlow("")
    val password2: StateFlow<String> = _password2.asStateFlow()


    private val _date = MutableStateFlow("")
    val date: StateFlow<String> = _date.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker


    private val _isLoadingR = MutableStateFlow(false)
    val isLoadingR: StateFlow<Boolean> = _isLoadingR.asStateFlow()

    private val _registerEnable = MutableStateFlow(false)
    val registerEnable: StateFlow<Boolean> = _registerEnable.asStateFlow()

    //para almacenar los errores-------------------------------
    private val _emailError = MutableStateFlow("")
    val emailError: StateFlow<String?> get() = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow("")
    val passwordError: StateFlow<String?> get() = _passwordError.asStateFlow()

    private val _passwordError2 = MutableStateFlow("")
    val passwordError2: StateFlow<String?> get() = _passwordError2.asStateFlow()

    private val _dateError = MutableStateFlow("")
    val dateError: StateFlow<String?> get() = _dateError.asStateFlow()

   // private val _isFormValid = MutableStateFlow<Boolean>(false)
    //FUNCIONES------------------------------------------------------------------------

    //validar el correo y contraseñas -----------------------------------
    private fun validEmail(email: String): Boolean  = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    private fun validPassword(password: String): Boolean = password.length > 8
    private fun passwordsSame(password: String, password2: String): Boolean = password == password2

    fun onLoginChanges(email: String, password: String, password2: String) {
        _email.value = email
        _password.value = password
        _password2.value = password2
        _registerEnable.value = (validEmail(email) && validPassword(password)) and (passwordsSame(password, password2))
    }

    fun dateSeleccionada(anio: Int, mes: Int, dia: Int) {
        val fechaFormateada = String.format("%02d/%02d/%04d", dia, mes + 1, anio)
        _date.value = fechaFormateada
        _showDatePicker.value = false
    }

    fun showMenuDate() {
        _showDatePicker.value = true
    }
    /*fun setEmail(email: String) {
        _email.value = email
        if (email.isNotEmpty()) _emailError.value = null.toString()
    }

    fun setPassword(password: String) {
        _password.value = password
        if (password.isNotEmpty()) _passwordError.value = null.toString()
    }

    fun setPassword2(password2: String) {
        _password2.value = password2
        if (password2.isNotEmpty()) _passwordError2.value = null.toString()
    }*/


    /* fun validateOnSubmit(): Boolean {
         val email = _email.value.orEmpty()
         val password = _password.value.orEmpty()
         val date = _date.value.orEmpty()

         _emailError.value = if (email.isEmpty()) "El correo no puede estar vacío" else null.toString()
         _passwordError.value = if (password.isEmpty()) "La contraseña no puede estar vacía" else null.toString()
         _dateError.value = if (date.isEmpty() || date == "d-M-yyyy") "Debes seleccionar una fecha" else null.toString()


         val isValid = email.isNotEmpty() && password.isNotEmpty() && date.isNotEmpty() && date != "d-M-yyyy"
         _isFormValid.value = isValid
         return isValid
     }*/
}
