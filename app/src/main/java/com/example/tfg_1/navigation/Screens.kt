package com.example.tfg_1.navigation

sealed class Screens(val route: String) {
    data object Login : Screens("login")
    data object Register : Screens("register")
    data object Tasks : Screens("tasks")
    data object Home : Screens("home")
    data object Splash : Screens("splash") //mantener sesion logueada
    data object Settings : Screens("settings")
    data object Expenses : Screens("expenses")
    data object Chat : Screens("chat")
}