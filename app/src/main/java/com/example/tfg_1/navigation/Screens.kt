package com.example.tfg_1.navigation

sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Register : Screens("register")
    object Home : Screens("home")
}