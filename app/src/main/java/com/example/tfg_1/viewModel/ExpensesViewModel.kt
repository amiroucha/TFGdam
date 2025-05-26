package com.example.tfg_1.viewModel

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_1.model.ExpensesModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import com.example.tfg_1.repositories.UserRepository
import java.util.*

class ExpensesViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    var gastos  by mutableStateOf<List<ExpensesModel>>(emptyList())
        private set

    //lista de gastos filtrados por usuario
    var gastosFiltrados by mutableStateOf<List<ExpensesModel>>(emptyList())
        private set

    var loading by mutableStateOf(true)
        private set

    private var listenerRegistration: ListenerRegistration? = null
    private var homeIdBD: String? = null

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    // Filtros de usuario
    private var usuarioFiltrado by mutableStateOf<String?>(null)

    var usuarios by mutableStateOf<List<String>>(emptyList())

    init {
        escucharUsuario()
    }
    //para logs
    companion object {
        private const val TAG = "ExpensesViewModel"
    }

    private fun actualizarGastosFiltrados() {
        gastosFiltrados = gastos.filter { gasto ->
            usuarioFiltrado == null || gasto.asignadoA == usuarioFiltrado
        }
    }

    fun modificaUsuarioFiltrado(usuario: String?) {
        usuarioFiltrado = usuario
        actualizarGastosFiltrados()
    }

    //escuchar cambios en el usuario
    //si el homeID de ese usuario no es = al que habia guardado se act
    private fun escucharUsuario() {
        val uid = auth.currentUser?.uid

        if (uid == null) {//en caso de que el usuario no este logueado
            loading = false
            Log.w(TAG, "Usuario no autenticado")
            viewModelScope.launch { _uiEvent.emit(UiEvent.Error("Usuario no autenticado")) }
            return
        }
        userRepository.escucharHomeIdUsuarioActual { homeId ->
            //si es null me voy
            if (homeId == null) {
                loading = false
                viewModelScope.launch {
                    _uiEvent.emit(UiEvent.Error("Error leyendo usuario o homeId"))
                }
                return@escucharHomeIdUsuarioActual
            }

            //si no es igual actualizo
            if (homeIdBD != homeId) {
                homeIdBD = homeId
                Log.d(TAG, "homeId actualizado, llamo a escucharGastos()")
                //recojo los gatsos de ese hogar
                listenerRegistration?.remove()
                listenerRegistration = userRepository.escucharGastos(homeId) { nuevosGastos ->
                    gastos = nuevosGastos
                    actualizarGastosFiltrados()
                    loading = false
                }

                //cargo los usuarios que tiene
                viewModelScope.launch {
                    try{
                        val miembros = userRepository.getMembersByHomeId(homeId)
                        usuarios = miembros.map { it.name }
                    }catch (e: Exception) {
                        Log.e(TAG, "Error al cargar usuarios", e)
                        _uiEvent.emit(UiEvent.Error("Error al cargar usuarios: ${e.localizedMessage}"))
                    }
                }
            }
        }
    }

    //limpiar el recordatorio
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
    //añadir un gasto
    suspend fun aniadirGastoVM(
        categoria: String,
        descripcion: String,
        importe: Double,
        fecha: Date,
        asignadoA: String): Boolean
    {
        val homeId = homeIdBD ?: return false
        val gasto = ExpensesModel(
            categoria = categoria.lowercase(),
            descripcion = descripcion,
            importe = importe,
            fecha = fecha,
            asignadoA = asignadoA
        )
        val success = userRepository.addExpense(homeId, gasto)
        if (success){
            _uiEvent.emit(UiEvent.Added)
            return true
        }
        else {
            _uiEvent.emit(UiEvent.Error("Error al guardar gasto"))
            return false
        }
    }

    //eliminar un gasto
    fun eliminarGasto(gasto: ExpensesModel) {
        viewModelScope.launch {
            val success = userRepository.deleteExpense(homeIdBD ?: return@launch, gasto.id)
            if (!success) _uiEvent.emit(UiEvent.Error("Error al eliminar gasto"))
        }
    }


    sealed interface UiEvent {
        data object Added : UiEvent
        data class Error(val msg: String) : UiEvent
    }
}
