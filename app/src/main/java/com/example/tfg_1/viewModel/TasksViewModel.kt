package com.example.tfg_1.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.tfg_1.model.TasksModel


class TasksViewModel {

    //lista de tareas
    private val _tareasList = mutableStateListOf<TasksModel>()
    val tareasList: List<TasksModel> = _tareasList

    //funcion para añadir la tarea en la lista
    fun agregarTarea(titulo: String, fecha: String, asignadoA: String) {
         val nuevaTarea = TasksModel(
             id = if (_tareasList.isEmpty()) 1 else _tareasList.maxOf { it.id } + 1,
             titulo = titulo,
             fecha = fecha,
             asignadoA = asignadoA,
             completada = false
         )
        _tareasList.add(nuevaTarea)

    }
    //cambiar el valor de hecho o no
    fun comprobarEstadoTarea(tarea: TasksModel) {
        val index = _tareasList.indexOfFirst { it.id == tarea.id }
        if (index != -1) {
            _tareasList[index] = tarea.copy(completada = !tarea.completada)
        }
    }
    //crea una lista que cumplan con x condicion
    fun tareasPendientes(): List<TasksModel> {
        val tareas = tareasList.filter { !it.completada }
        if (tareas.isEmpty()) {
            Log.d("TasksViewModel", "No hay tareas pendientes")
        }
        return tareas
    }
    fun tareasCompletadas(): List<TasksModel> {
        val tareas = tareasList.filter { it.completada }
        if (tareas.isEmpty()) {
            Log.d("TasksViewModel", "No hay tareas completadas")
        }
        return tareas
    }
}
