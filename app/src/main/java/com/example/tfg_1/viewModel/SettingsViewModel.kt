package com.example.tfg_1.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_1.model.UserModel
import com.example.tfg_1.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _homeId = MutableStateFlow("")
    val homeId: StateFlow<String> = _homeId.asStateFlow()


    private val _members = MutableStateFlow<List<UserModel>>(emptyList())
    val members: StateFlow<List<UserModel>> = _members.asStateFlow()

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Rest)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    private fun loadHome() = viewModelScope.launch {
        val user = repository.getCurrentUser()
        if (user == null) { //no autenticado
            _uiState.value = SettingsUiState.Error("Usuario no autenticado")
            return@launch
        }

        try {
            val doc = repository.getUserDoc(user.uid)
            val hid = doc.getString("homeId").orEmpty()
            _homeId.value = hid

            if (hid.isBlank()) { //no tiene hogarId
                _members.value = emptyList()
                _uiState.value = SettingsUiState.NoHome
                return@launch
            }
            //recojo ususarios según idHogar
            val membersList = repository.getMembersByHomeId(hid)
            _members.value = membersList
            _uiState.value = SettingsUiState.HasHome//act valor de estado

        } catch (e: Exception) {
            _uiState.value = SettingsUiState.Error(e.message)
        }
    }

    fun clearCurrentHome() = viewModelScope.launch {
        if (_homeId.value.isBlank()) return@launch

        _uiState.value = SettingsUiState.Loading
        val user = repository.getCurrentUser()//consigo usuario actual
        if (user == null) {
            _uiState.value = SettingsUiState.Error("Usuario no autenticado")
            return@launch
        }

        try { //borro idHome y vacío valores y cambio estado de usuario
            repository.updateUserHomeId(user.uid, "")
            _homeId.value = ""
            _members.value = emptyList()
            _uiState.value = SettingsUiState.LeftHome
        } catch (e: Exception) {
            _uiState.value = SettingsUiState.Error(e.message)
        }
    }

    sealed interface SettingsUiState {
        data object Rest : SettingsUiState           // reposo
        data object Loading : SettingsUiState        // operando
        data object HasHome : SettingsUiState        // posee hogar
        data object NoHome : SettingsUiState         // sin hogar
        data object LeftHome : SettingsUiState       // acaba de salir del hogar
        data class Error(val msg: String?) : SettingsUiState
    }
}