package com.example.tfg_1.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tfg_1.ui.ui.HomeScreen
import com.example.tfg_1.ui.ui.LoginScreen
import com.example.tfg_1.ui.ui.RegisterScreen
import com.example.tfg_1.viewModel.HomeViewModel
import com.example.tfg_1.viewModel.LoginViewModel
import com.example.tfg_1.viewModel.RegisterViewModel

@Composable
fun NavigationWrapper() {

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.Login.route) {
        composable(Screens.Login.route) {
            val viewModel = LoginViewModel()
            LoginScreen(
                viewModel = viewModel,
                navController
            )
        }
        composable(Screens.Register.route) {
            val viewModel = RegisterViewModel()
            RegisterScreen(viewModel = viewModel,navController)
        }
        composable(Screens.Home.route) {
            val viewModel = HomeViewModel()
            HomeScreen(viewModel = viewModel,navController)
        }
    }
}