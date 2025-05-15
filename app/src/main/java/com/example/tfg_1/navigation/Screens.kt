package com.example.tfg_1.navigation

sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Register : Screens("register")
    object Tasks : Screens("tasks")
    object Home : Screens("home")
    object Splash : Screens("splash") //mantener sesion logueada
    object Settings : Screens("settings")
}