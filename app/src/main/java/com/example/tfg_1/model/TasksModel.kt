package com.example.tfg_1.model

data class TasksModel (
    val id: Int,
    val titulo: String,
    val fecha: String,
    val asignadoA: String,
    val completada: Boolean
)