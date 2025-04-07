package com.example.tfg_1.navigation

import kotlinx.serialization.Serializable

sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Register : Screens("register")
}