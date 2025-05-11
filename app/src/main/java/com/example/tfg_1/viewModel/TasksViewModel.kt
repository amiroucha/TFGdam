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


    init {
        cargarTareasDesdeFirebase()
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

    //funcion para añadir la tarea en la lista
    /*  fun agregarTarea(titulo: String, fecha: String, asignadoA: String) {
           val nuevaTarea = TasksModel(
               id = if (_tareasList.isEmpty()) 1 else _tareasList.maxOf { it.id } + 1,
               titulo = titulo,
               fecha = fecha,
               asignadoA = asignadoA,
               completada = false,
               homeId = homeId  //se usa para filtrar y cargar tareas
          )
          _tareasList.add(nuevaTarea)

      }*/

    /*
    //cambiar el valor de hecho o no
    fun comprobarEstadoTarea(tarea: TasksModel) {
        val index = _tareasList.indexOfFirst { it.id == tarea.id }
        if (index != -1) {
            _tareasList[index] = tarea.copy(completada = !tarea.completada)
        }
    }
    //crea una lista que cumplan con x condicion
    fun tareasPendientes(): List<TasksModel> {
        val tareas = tareasList.filter { !it.completada && it.homeId == homeIdActual}
        if (tareas.isEmpty()) {
            Log.d("TasksViewModel", "No hay tareas pendientes")
        }
        return tareas
    }
    fun tareasCompletadas(): List<TasksModel> {
        val tareas = tareasList.filter { it.completada && it.homeId == homeIdActual }
        if (tareas.isEmpty()) {
            Log.d("TasksViewModel", "No hay tareas completadas")
        }
        return tareas
    }*/
}
