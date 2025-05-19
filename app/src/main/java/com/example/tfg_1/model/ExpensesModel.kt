package com.example.tfg_1.model

import java.util.*

data class ExpensesModel  (
    val id: String = "",
    val categoria: String = "",
    val creadoPor: String = "",
    val descripcion: String = "",
    val fecha: Date = Date(),
    val homeId:String = "",
    val importe: Double = 0.0,

)