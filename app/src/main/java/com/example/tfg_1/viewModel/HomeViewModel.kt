package com.example.tfg_1.viewModel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.tfg_1.R
import com.example.tfg_1.model.UserModel
import com.example.tfg_1.repositories.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class HomeViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    // Estado de navegación
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Variables de tipo home
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()

    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()

    // Lista de miembros del hogar
    private val _members = MutableStateFlow<List<UserModel>>(emptyList())
    val members: StateFlow<List<UserModel>> = _members.asStateFlow()

    // Actualizar datos locales
    fun changeName(at: String) { _name.value = at }
    fun changeAdress(at: String) { _address.value = at }
    fun actCode(at: String) { _code.value = at }

    init {
        loadUser()
    }

    // Carga inicial del usuario y ver si tiene un hogar
    fun loadUser() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val firebaseUser = repository.getCurrentUser()
            if (firebaseUser == null) {
                _uiState.value = UiState.NotLogged
                return@launch
            }

            try {
                val doc = repository.getUserDoc(firebaseUser.uid)
                val homeId = doc.getString("homeId").orEmpty()

                if (homeId.isBlank()) {
                    _uiState.value = UiState.NotHome
                    _members.value = emptyList()
                } else {
                    _uiState.value = UiState.HasHome(homeId)
                    _members.value = repository.getMembersByHomeId(homeId)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage)
            }
        }
    }

    // Crear hogar en base datos y asignarlo al usuario
    fun createHome(context: Context) {
        viewModelScope.launch {
            val homeName = name.value.trim()
            val addressValue = address.value.trim()
            if (homeName.isEmpty()) return@launch

            try {
                val newHomeId = repository.createHome(homeName, addressValue)
                repository.updateUserHomeId(repository.getCurrentUser()?.uid ?: "", newHomeId)
                loadUser() // recarga usuario para actualizar estado
            } catch (e: Exception) {
                _uiState.value = UiState.Error(context.getString(R.string.no_se_pudo_crear_el_hogar))
            }
        }
    }

    // Unir a un hogar existente por un código
    fun joinHome(context: Context) {
        viewModelScope.launch {
            val codeVal = code.value.trim()
            if (codeVal.isEmpty()) return@launch

            try {
                val homeDoc = repository.getHomeById(codeVal)
                if (homeDoc.exists()) {
                    repository.updateUserHomeId(repository.getCurrentUser()?.uid ?: "", codeVal)
                    loadUser() // recarga usuario para actualizar estado
                } else {
                    _uiState.value = UiState.Error(context.getString(R.string.codigo_de_hogar_invaldo))
                    Toast.makeText(context, context.getString(R.string.codigo_de_hogar_invaldo), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(context.getString(R.string.error_al_buscar_el_hogar))
            }
        }
    }

    sealed class UiState {
        object Loading : UiState()       // comprobando usuario y hogar
        object NotLogged : UiState()     // no está logueado
        object NotHome : UiState()       // sin hogar y logueado
        data class HasHome(val homeId: String) : UiState() // si tiene hogar
        data class Error(val message: String?) : UiState() // error
    }
}
