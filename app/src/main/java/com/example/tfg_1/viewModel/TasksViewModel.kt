package com.example.tfg_1.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_1.model.TasksModel
import com.example.tfg_1.repositories.UserRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import com.example.tfg_1.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.*


class TasksViewModel: ViewModel()
{
    private val userRepository = UserRepository()
    private var homeId : String? = null

    private val _loading = mutableStateOf(true)
    val loading: State<Boolean> get() = _loading

    //lista de tareas
    private val _tareasList = mutableStateListOf<TasksModel>()
    private val tareasList: List<TasksModel> = _tareasList

    private val _usuarios = mutableStateListOf<String>()
    val usuarios: List<String> get() = _usuarios

    var usuarioFiltrado by mutableStateOf<String?>(null)//null=all users

    private var tareasListener: ListenerRegistration? = null

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    init {
        viewModelScope.launch {
            _loading.value = true
            homeId = userRepository.getCurrentUserHomeId()
            cargarUsuarios()
            escucharTareas()
        }
    }

    private suspend fun cargarUsuarios() {
        homeId?.let { it ->
            val users = userRepository.getMembersByHomeId(it)
            _usuarios.clear()
            _usuarios.addAll(users.map { it.name })
        }
    }

    private fun escucharTareas() {
        homeId?.let { id ->
            tareasListener = userRepository.escucharTareas(id) { tareas ->
                _tareasList.clear()
                _tareasList.addAll(tareas)
                _loading.value = false //quito spinner cuando esten las tareas
            }
        }
    }

    fun modificaUsuarioFiltrado(usuario: String?) {
        usuarioFiltrado = usuario
    }

    fun tareasPendientes(): List<TasksModel> =
        tareasList.filter { !it.completada && (usuarioFiltrado == null || it.asignadoA == usuarioFiltrado) }

    fun tareasCompletadas(): List<TasksModel> =
        tareasList.filter { it.completada && (usuarioFiltrado == null || it.asignadoA == usuarioFiltrado) }

    fun agregarTarea(titulo: String, fecha: String, asignadoA: String) {
        val tarea = TasksModel(
            id = UUID.randomUUID().toString(),
            titulo = titulo,
            fecha = fecha,
            asignadoA = asignadoA,
            completada = false,
            homeId = homeId ?: return
        )

        viewModelScope.launch {
            userRepository.agregarTarea(tarea)
        }
    }

    fun comprobarEstadoTarea(tarea: TasksModel, context: Context, deCompletadas: Boolean = false) {
        viewModelScope.launch {
            val usuarioActual = userRepository.getCurrentUserName().trim().lowercase()
            val asignado = tarea.asignadoA.trim().lowercase()

            // solo restringir si se intenta desmarcar una completada
            if (deCompletadas && asignado != usuarioActual) {
                _uiEvent.emit(UiEvent.Error(context.getString(R.string.nopermiso_modificar_tarea)))
                return@launch
            }

            val nueva = tarea.copy(completada = !tarea.completada)
            userRepository.actualizarTarea(nueva)
        }
    }

    fun eliminarTarea(tarea: TasksModel, context: Context, deCompletadas: Boolean = false) {
        viewModelScope.launch {
            val usuarioActual = userRepository.getCurrentUserName().trim().lowercase()
            val asignado = tarea.asignadoA.trim().lowercase()

            // Si tarea está asignado a != del usuario actual
            // no permitir eliminar
            if (asignado != usuarioActual && deCompletadas ) {
                _uiEvent.emit(UiEvent.Error(context.getString(R.string.nopermiso_eliminar_tarea)))
                return@launch
            }

            userRepository.eliminarTarea(homeId ?: return@launch, tarea.id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tareasListener?.remove()
    }

    sealed interface UiEvent {
        data object Added : UiEvent
        data class Error(val msg: String) : UiEvent
        data class Emit(val msg:String):UiEvent
    }
}
