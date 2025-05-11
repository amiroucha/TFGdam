package com.example.tfg_1.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.tfg_1.model.TasksModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class TasksViewModel(private val homeId: String): ViewModel()
{
    //lista de tareas
    private val _tareasList = mutableStateListOf<TasksModel>()
    val tareasList: List<TasksModel> = _tareasList

    private val _usuarios = mutableStateListOf<String>()
    val usuarios: List<String> get() = _usuarios

    init {
        cargarTareasDesdeFirebase()
        cargarUsuariosDesdeFirebase()

    }

    private fun cargarTareasDesdeFirebase() {
        FirebaseFirestore.getInstance().collection("tareas")
            .whereEqualTo("homeId", homeId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("TasksViewModel", "Error al obtener tareas: ${exception.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _tareasList.clear()
                    for (doc in snapshot) {
                        try {
                            // Intentamos convertir el documento en un objeto TasksModel
                            val tarea = doc.toObject(TasksModel::class.java)
                            // Verificamos si la conversión fue exitosa
                            if (tarea != null) {
                                _tareasList.add(tarea)
                            } else {
                                Log.e("TasksViewModel", "No se pudo convertir el documento: ${doc.id}")
                            }
                        } catch (e: Exception) {
                            // Si ocurre un error durante la conversión, lo registramos
                            Log.e("TasksViewModel", "Error al convertir el documento ${doc.id}: ${e.message}")
                        }
                    }
                }
            }
    }

    private fun cargarUsuariosDesdeFirebase() {
        FirebaseFirestore.getInstance().collection("usuarios")
            .whereEqualTo("homeId", homeId) //usuarios de ese = hogar
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

    fun agregarTarea(titulo: String, fecha: String, asignadoA: String) {
        val nuevaTarea = TasksModel(
            id = UUID.randomUUID().toString(),
            titulo = titulo,
            fecha = fecha,
            asignadoA = asignadoA,
            completada = false,
            homeId = homeId
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
        tareasList.filter { !it.completada }

    fun tareasCompletadas(): List<TasksModel> =
        tareasList.filter { it.completada }
}
