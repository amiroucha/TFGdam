package com.example.tfg_1.viewModel

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

    fun comprobarEstadoTarea(tarea: TasksModel) {
        val nueva = tarea.copy(completada = !tarea.completada)
        viewModelScope.launch {
            userRepository.actualizarTarea(nueva)
        }
    }

    fun eliminarTarea(tarea: TasksModel) {
        viewModelScope.launch {
            userRepository.eliminarTarea(homeId ?: return@launch, tarea.id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tareasListener?.remove()
    }

}
