package com.example.tfg_1.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_1.model.ExpensesModel
import com.example.tfg_1.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class ExpensesViewModel : ViewModel() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var gastos  by mutableStateOf<List<ExpensesModel>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set

    private var listenerRegistration: ListenerRegistration? = null
    private var homeIdBD: String? = null

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    sealed interface UiEvent {
        object Added : UiEvent
        data class Error(val msg: String) : UiEvent
    }

    companion object {
        private const val TAG = "ExpensesViewModel"
    }

    init {
        Log.d(TAG, "init -> escucharUsuario()")
        escucharUsuario()
    }

    /** Paso 1: Escuchar cambios en el documento del usuario */
    private fun escucharUsuario() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            loading = false
            Log.w(TAG, "Usuario no autenticado")
            viewModelScope.launch { _uiEvent.emit(UiEvent.Error("Usuario no autenticado")) }
            return
        }

        db.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    loading = false
                    Log.e(TAG, "Error leyendo usuario", error)
                    viewModelScope.launch {
                        _uiEvent.emit(UiEvent.Error("Error leyendo usuario: ${error?.message}"))
                    }
                    return@addSnapshotListener
                }

                val homeId = snapshot.getString("homeId")
                Log.d(TAG, "Snapshot usuario -> homeId=$homeId")
                if (homeId.isNullOrBlank()) {
                    loading = false
                    Log.w(TAG, "homeId no encontrado en el perfil")
                    viewModelScope.launch {
                        _uiEvent.emit(UiEvent.Error("homeId no encontrado en el perfil"))
                    }
                    return@addSnapshotListener
                }

                if (homeIdBD != homeId) {
                    homeIdBD = homeId
                    Log.d(TAG, "homeIdBD actualizado -> escucharGastos()")
                    escucharGastos(homeId)
                }
            }
    }

    /** Paso 2: Escuchar gastos del hogar */
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
        fecha: Date
    ): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val homeId = homeIdBD ?: return false

        val data = mapOf(
            "categoria"  to categoria.lowercase(),
            "creadoPor"  to uid,
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
}
