package com.example.tfg_1.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tfg_1.ui.ui.LoginScreen
import com.example.tfg_1.ui.ui.RegisterScreen
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
            val viewModelR = RegisterViewModel()
            RegisterScreen(viewModel =viewModelR)
        }
    }
}