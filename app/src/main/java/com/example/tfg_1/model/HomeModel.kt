package com.example.tfg_1.model

data class HomeModel(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val createdIn: Long = System.currentTimeMillis(),
)
