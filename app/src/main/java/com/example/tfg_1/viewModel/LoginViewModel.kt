package com.example.tfg_1.viewModel

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModel
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.tfg_1.R
import com.example.tfg_1.navigation.Screens
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class LoginViewModel : ViewModel() {
    //private val _navController = navController
    private lateinit var credentialManager: androidx.credentials.CredentialManager

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


    private fun validateOnSubmit(): Boolean {
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

        return _emailError.value == null && _passwordError.value == null
    }

    private fun checkAuthStatus() {
        _authState.value = if (auth.currentUser != null) AuthState.Authenticated else AuthState.Unauthenticated
    }

    fun sendResetPassword(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _passwordResetMessage.value = "Se ha enviado un correo para restablecer tu contraseña."
                } else {
                    _passwordResetMessage.value = "No se pudo enviar el correo. Verifica el email."
                }
            }
    }
    fun clearPasswordResetMessage() {
        _passwordResetMessage.value = null
    }
    fun setPasswordResetError(message: String) {
        _passwordResetMessage.value = message
    }

    fun login(navController: NavController) {
        val email = _email.value
        val password = _password.value
        if (validateOnSubmit()) {
            _isLoading.value = true
            _authState.value = AuthState.Loading

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    _isLoading.value = false
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Authenticated

                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            // Llamada asíncrona para obtener el homeId
                            viewModelScope.launch {
                                val homeId = getHomeId(currentUser)

                                // Después de obtener el homeId, realizar la navegación
                                if (homeId != null) {
                                    // Si tiene un homeId, ir a Tasks
                                    navController.navigate(Screens.Tasks.route) {
                                        popUpTo(Screens.Login.route) { inclusive = true }
                                    }
                                } else {
                                    // Si no tiene homeId, ir a Home
                                    navController.navigate(Screens.Home.route) {
                                        popUpTo(Screens.Login.route) { inclusive = true }
                                    }
                                }
                            }
                        }
                    } else {
                        val errorMsg = task.exception?.message ?: "ERROR. Algo fue mal"
                        _authState.value = AuthState.Error(errorMsg)
                    }
                }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }



    private suspend fun getHomeId(user: FirebaseUser?): String? {
        return try {
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("usuarios").document(user?.uid ?: "")
            val documentSnapshot = userDocRef.get().await()

            // Si existe el documento, intenta obtener el homeId
            if (documentSnapshot.exists()) {
                return documentSnapshot.getString("homeId")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }


    fun loginGoogle(context: Context, navController: NavController) {
        val auth = FirebaseAuth.getInstance() // inicializar FirebaseAuth

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // false para cuenta de Google
            .setServerClientId(context.getString(R.string.idWeb))
            .setAutoSelectEnabled(false) // No selecciona automáticamente una cuenta
            .build()

        // solicitud para obtener las credenciales
        val request: androidx.credentials.GetCredentialRequest = androidx.credentials.GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            credentialManager = androidx.credentials.CredentialManager.create(context)
            try {
                // cojo las credenciales
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                // extraer token de Google
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                // crea una credencial de Firebase usndo google
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

                // Intento autenticar al user
                val authResult = auth.signInWithCredential(firebaseCredential).await()

                // Si la autenticación es exitosa
                if (authResult != null) {
                    // Recojo datos del usuario
                    val user = auth.currentUser
                    val userUid = user?.uid
                    val userEmail = user?.email
                    val userMap = hashMapOf(
                        "email" to (userEmail ?: ""),
                        "uid" to userUid,
                    )

                    // Guardar los datos del usuario en la base de datos
                    userUid?.let {
                        FirebaseFirestore.getInstance()
                            .collection("usuarios")
                            .document(it) // UID en Firebase =  ID del documento de ese user
                            .set(userMap, SetOptions.merge()) //para que se acople pero nome sobreescriba
                            .addOnSuccessListener {
                                Toast.makeText(context, context.getString(R.string.login_exitoso), Toast.LENGTH_LONG).show()

                                // Después de guardar los datos, revisamos si tiene un homeId

                                viewModelScope.launch {
                                    val currentUser = auth.currentUser
                                    val homeId = getHomeId(currentUser)


                                    // Después de obtener el homeId, realizar la navegación
                                    if (homeId != null) {
                                        //Toast.makeText(context, homeId, Toast.LENGTH_LONG).show()
                                        // Si tiene un homeId, ir a Tasks
                                        navController.navigate(Screens.Tasks.route) {
                                            popUpTo(Screens.Login.route) { inclusive = true }
                                        }
                                    } else {
                                        // Si no tiene homeId, ir a Home
                                        navController.navigate(Screens.Home.route) {
                                            popUpTo(Screens.Login.route) { inclusive = true }
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                // Error al guardar los datos
                                Toast.makeText(context, "Error al guardar los datos del usuario: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    // Error en autenticación
                    Toast.makeText(context, context.getString(R.string.error_login_google), Toast.LENGTH_LONG).show()
                }
            } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                // Error al obtener las credenciales
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }


    sealed class AuthState {
        data object Authenticated : AuthState()
        data object Unauthenticated : AuthState()
        data object Loading : AuthState()
        data class Error(val error: String) : AuthState()
    }
}



