package com.example.tfg_1.model

data class TasksModel(
    val id: String= "",
    val titulo: String= "",
    val fecha: String= "",
    val asignadoA: String= "",
    val completada: Boolean = false,
    val homeId: String= ""
)