package com.example.tfg_1.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.tfg_1.model.TasksModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class TasksViewModel: ViewModel()
{
    private var homeId : String? = null
    //lista de tareas
    private val _tareasList = mutableStateListOf<TasksModel>()
    private val tareasList: List<TasksModel> = _tareasList

    private val _usuarios = mutableStateListOf<String>()
    val usuarios: List<String> get() = _usuarios

    private var usuarioFiltrado by mutableStateOf<String?>(null)//null=all users


    init {
        obtenerHomeIdYDatos()
    }

    private fun obtenerHomeIdYDatos() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("usuarios")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val homeIdResult = document.getString("homeId")
                    if (!homeIdResult.isNullOrEmpty()) {
                        homeId = homeIdResult
                        cargarTareasDesdeFirebase()
                        cargarUsuariosDesdeFirebase()
                    } else {
                        Log.e("TasksViewModel", "homeId no encontrado")
                    }
                }
                .addOnFailureListener {
                    Log.e("TasksViewModel", "Error al obtener homeId: ${it.message}")
                }
        }
    }
    private fun cargarTareasDesdeFirebase() {
        val idcasa = homeId
        FirebaseFirestore.getInstance().collection("tareas")
            .whereEqualTo("homeId", idcasa)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("TasksViewModel", "Error al obtener tareas: ${exception.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _tareasList.clear()
                    for (doc in snapshot) {
                        try {
                            val tarea = doc.toObject(TasksModel::class.java)
                            if (tarea != null) {
                                _tareasList.add(tarea)
                            }
                        } catch (e: Exception) {
                            Log.e("TasksViewModel", "Error al convertir tarea: ${e.message}")
                        }
                    }
                }
            }
    }

    private fun cargarUsuariosDesdeFirebase() {
        val idcasa = homeId ?: return
        FirebaseFirestore.getInstance().collection("usuarios")
            .whereEqualTo("homeId", idcasa)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("TasksViewModel", "Error al cargar usuarios: ${exception.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _usuarios.clear()
                    for (doc in snapshot.documents) {
                        val nombreUsuario = doc.getString("name")
                        if (!nombreUsuario.isNullOrBlank()) {
                            _usuarios.add(nombreUsuario)
                        }
                    }
                }
            }
    }

    fun modificaUsuarioFiltrado(usuario: String?) {
        usuarioFiltrado = usuario
    }

    fun agregarTarea(titulo: String, fecha: String, asignadoA: String) {
        val idcasa = homeId
        val nuevaTarea = TasksModel(
            id = UUID.randomUUID().toString(),
            titulo = titulo,
            fecha = fecha,
            asignadoA = asignadoA,
            completada = false,
            homeId = idcasa.toString()
        )

        FirebaseFirestore.getInstance().collection("tareas")
            .document(nuevaTarea.id)
            .set(nuevaTarea)
    }


    fun comprobarEstadoTarea(tarea: TasksModel) {
        val nuevaTarea = tarea.copy(completada = !tarea.completada)
        FirebaseFirestore.getInstance().collection("tareas")
            .document(tarea.id)
            .set(nuevaTarea)
    }

    fun tareasPendientes(): List<TasksModel> =
        tareasList.filter {  tarea ->
            !tarea.completada &&
            (usuarioFiltrado == null || tarea.asignadoA == usuarioFiltrado)
        }

    fun tareasCompletadas(): List<TasksModel> =
        tareasList.filter  { tarea ->
            tarea.completada &&
            (usuarioFiltrado == null || tarea.asignadoA == usuarioFiltrado)
        }
}
