package com.example.tfg_1.viewModel
import android.content.Context
import android.util.Patterns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.tfg_1.R
import com.example.tfg_1.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RegisterViewModel(navController: NavController) : ViewModel() {
    private val _navController = navController
    private lateinit var auth: FirebaseAuth
    //val context = LocalContext.current
    //----------------------------------------------------------------------

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _password2 = MutableStateFlow("")
    val password2: StateFlow<String> = _password2.asStateFlow()

    private val _birthdate = MutableStateFlow("")
    val birthdate: StateFlow<String> = _birthdate.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker
    //----------------------------------------------------------------------
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    //----------------------------------------------------------------------

    private val _isLoadingR = MutableStateFlow(false)
    val isLoadingR: StateFlow<Boolean> = _isLoadingR.asStateFlow()

    private val _registerEnable = MutableStateFlow(false)
    val registerEnable: StateFlow<Boolean> = _registerEnable.asStateFlow()

    //para almacenar los errores-------------------------------
    private val _emailError = MutableStateFlow("")
    val emailError: StateFlow<String> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow("")
    val passwordError: StateFlow<String> = _passwordError.asStateFlow()

    private val _passwordError2 = MutableStateFlow("")
    val passwordError2: StateFlow<String> = _passwordError2.asStateFlow()

    private val _passwordsame = MutableStateFlow("")
    val passwordsame: StateFlow<String> = _passwordsame.asStateFlow()

    private val _dateError = MutableStateFlow("")
    val dateError: StateFlow<String> = _dateError.asStateFlow()

    private val _nameError = MutableStateFlow("")
    val nameError: StateFlow<String> = _nameError.asStateFlow()

    //FUNCIONES------------------------------------------------------------------------

    //validar el correo y contraseñas -----------------------------------
    private fun validEmail(email: String): Boolean  = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    private fun validPassword(password: String): Boolean = password.length >= 8
    private fun passwordsSame(password: String, password2: String): Boolean = password == password2


    fun onLoginChanges(email: String, password: String, password2: String,name: String) {
        _email.value = email
        _password.value = password
        _password2.value = password2
        _name.value = name
        _registerEnable.value = (validEmail(email) && validPassword(password)) and (passwordsSame(password, password2) && name.isNotEmpty())
    }

    fun dateSeleccionada(anio: Int, mes: Int, dia: Int) {
        val fechaFormateada = String.format("%02d/%02d/%04d", dia, mes + 1, anio)
        _birthdate.value = fechaFormateada
        _showDatePicker.value = false
    }

    fun showMenuDate() {
        _showDatePicker.value = true
    }
    private fun validateOnSubmit(): Boolean {
        val email = _email.value
        val password = _password.value
        val password2 = _password2.value
        val date = _birthdate.value
        val name = _name.value

        _emailError.value = when {
            email.isEmpty() -> "El correo no puede estar vacío"
            !validEmail(email) -> "Correo incorrecto"
            else -> ""
        }

        _passwordError.value = when {
            password.isEmpty() -> "La contraseña no puede estar vacía"
            !validPassword(password) -> "La contraseña debe tener más longitud"
            else -> ""
        }
        _passwordError2.value = when {
            password2.isEmpty() -> "La contraseña no puede estar vacía"
            !validPassword(password2) -> "La contraseña debe tener más longitud"
            else -> ""
        }
        _passwordsame.value = when {
            !passwordsSame(password, password2) -> "Las contraseñas deben ser iguales"
            else -> ""
        }

        _nameError.value = when {
            name.isEmpty() -> "El nombre no puede estar vacío"
            else -> ""
        }

        // Validación de la fecha de nacimiento
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateMini = dateFormat.parse("01/01/1900")
        val dateMaxi = Calendar.getInstance().time

        _dateError.value = when {
            date.isEmpty() -> "La fecha no puede estar vacía"
            else -> {
                try {
                    val selectedDate = dateFormat.parse(date)
                    if (selectedDate.before(dateMini) || selectedDate.after(dateMaxi)) {
                        "Fecha fuera de rango"
                    } else ""
                } catch (e: Exception) {
                    "Formato de fecha inválido"
                }
            }
        }

        return _emailError.value.isEmpty() &&
                _passwordError.value.isEmpty() &&
                _passwordError2.value.isEmpty() &&
                _passwordsame.value.isEmpty() &&
                _dateError.value.isEmpty() &&
                _nameError.value.isEmpty()
    }
    fun botonRegistro(context: Context){
        if (!validateOnSubmit()) return //si hay un dato incorrecto se sale

        _isLoadingR.value = true
        auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(_email.value, _password.value)
            .addOnCompleteListener { task ->
                _isLoadingR.value = false
                if (task.isSuccessful) {
                    val userUid = auth.currentUser?.uid

                    userUid?.let { id -> // si el id no es null
                        val userModel = UserModel(
                            id = id,
                            name = _name.value,
                            email = _email.value,
                            homeId = null,
                            birthDate = _birthdate.value
                        )
                        FirebaseFirestore.getInstance()
                            .collection("usuarios")
                            .document(id)
                            .set(userModel)
                            .addOnSuccessListener {
                               /* _navController.navigate("tasks") {
                                    popUpTo("register") { inclusive = true }
                                    popUpTo("login") { inclusive = true }
                                }*/
                            }.addOnFailureListener { e ->
                                _emailError.value = e.localizedMessage ?: context.getString(R.string.errorGuardarUsuario)
                            }
                    }

                } else {
                    // Mostrar error si hubo un problema
                    _emailError.value = task.exception?.localizedMessage ?: context.getString(R.string.erroRegistrar)
                }
            }
    }
}
