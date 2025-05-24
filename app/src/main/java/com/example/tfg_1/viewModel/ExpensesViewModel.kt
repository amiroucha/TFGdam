package com.example.tfg_1.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_1.model.ExpensesModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.State
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg_1.repositories.UserRepository
import java.util.*

class ExpensesViewModel : ViewModel() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()


    var gastos  by mutableStateOf<List<ExpensesModel>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set

    private var listenerRegistration: ListenerRegistration? = null
    private var homeIdBD: String? = null

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    // Filtros de usuario
    var usuarioFiltrado by mutableStateOf<String?>(null)
        private set

    var usuarios by mutableStateOf<List<String>>(emptyList())
        private set

    init {
        escucharUsuario()
    }
    //para logs
    companion object {
        private const val TAG = "ExpensesViewModel"
    }


    fun modificaUsuarioFiltrado(usuario: String?) {
        usuarioFiltrado = usuario
    }

    //escuchar cambios en el usuario
    //si el homeID de ese usuario no es = al que habia guardado se act
    private fun escucharUsuario() {
        val uid = auth.currentUser?.uid
        //en caso de que el usuario no este logueado
        if (uid == null) {
            loading = false
            Log.w(TAG, "Usuario no autenticado")
            viewModelScope.launch { _uiEvent.emit(UiEvent.Error("Usuario no autenticado")) }
            return
        }
        //
        db.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, error ->
                //en caso de error o documento no encontrado
                if (error != null || snapshot == null) {
                    loading = false //quito circularProgresion
                    Log.e(TAG, "Error leyendo usuario", error)
                    viewModelScope.launch {
                        _uiEvent.emit(UiEvent.Error("Error leyendo usuario: ${error?.message}"))
                    }
                    return@addSnapshotListener
                }
                //recojo el homeID
                val homeId = snapshot.getString("homeId")
                Log.d(TAG, "Snapshot usuario -> homeId=$homeId")
                //si no es correcto homeID
                if (homeId.isNullOrBlank()) {
                    loading = false
                    Log.w(TAG, "homeId no encontrado en el perfil")
                    viewModelScope.launch {
                        _uiEvent.emit(UiEvent.Error("homeId no encontrado en el perfil"))
                    }
                    return@addSnapshotListener
                }
                //si no es igual actualizo
                if (homeIdBD != homeId) {
                    homeIdBD = homeId
                    Log.d(TAG, "homeId actualizado, llamo a escucharGastos()")
                   //recojo los gatsos de ese hogar
                    escucharGastos(homeId)
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

    // Escuchar gastos del hogar
    private fun escucharGastos(homeId: String) {
        // Cancela listener anterior si lo hubiera
        listenerRegistration?.remove()

        listenerRegistration = db.collection("hogares").document(homeId)
            .collection("gastos")
            .whereEqualTo("homeId", homeId)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    loading = false
                    Log.e(TAG, "Error escuchando gastos", err)
                    viewModelScope.launch {
                        _uiEvent.emit(UiEvent.Error("Error escuchando gastos: ${err?.message}"))
                    }
                    return@addSnapshotListener
                }

                //convertir los nuevos gastos
                gastos = snap.documents.mapNotNull { d ->
                    d.toObject(ExpensesModel::class.java)?.copy(id = d.id)
                }
                loading = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    suspend fun aniadirGastoVM(
        categoria: String,
        descripcion: String,
        importe: Double,
        fecha: Date,
        asignadoA: String
    ): Boolean {
        val homeId = homeIdBD ?: return false

        val data = mapOf(
            "categoria"  to categoria.lowercase(),
            "asignadoA"  to asignadoA,
            "descripcion" to descripcion,
            "fecha"      to fecha,
            "homeId"     to homeId,
            "importe"    to importe
        )

        return try {
            db.collection("hogares").document(homeId)
                .collection("gastos")
                .add(data)
                .await()
            Log.i(TAG, "Gasto añadido correctamente")
            _uiEvent.emit(UiEvent.Added)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar gasto", e)
            _uiEvent.emit(UiEvent.Error("Error al guardar: ${e.localizedMessage}"))
            false
        }
    }
    //eliminar un gasto
    fun eliminarGasto(gasto: ExpensesModel) {
        val homeId = homeIdBD ?: return

        viewModelScope.launch {
            try {
                db.collection("hogares")
                    .document(homeId)
                    .collection("gastos")
                    .document(gasto.id)
                    .delete()
                    .await()

                Log.d(TAG, "Gasto eliminado correctamente")
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar gasto", e)
                _uiEvent.emit(UiEvent.Error("Error al eliminar gasto: ${e.localizedMessage}"))
            }
        }
    }


    val gastosFiltrados: List<ExpensesModel>
        get() = gastos.filter { gasto ->
        usuarioFiltrado == null || gasto.asignadoA == usuarioFiltrado
    }

    sealed interface UiEvent {
        object Added : UiEvent
        data class Error(val msg: String) : UiEvent
    }
}
