package com.example.tfg_1.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg_1.R
import com.example.tfg_1.ui.ui.HomeScreen
import com.example.tfg_1.ui.ui.LoginScreen
import com.example.tfg_1.ui.ui.RegisterScreen
import com.example.tfg_1.ui.ui.TasksScreen
import com.example.tfg_1.viewModel.HomeViewModel
import com.example.tfg_1.viewModel.LoginViewModel
import com.example.tfg_1.viewModel.RegisterViewModel
import com.example.tfg_1.viewModel.TasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()

    val loginViewModel: LoginViewModel = viewModel()
    val authState by loginViewModel.authState.collectAsState()

    val homeViewModel: HomeViewModel = viewModel()

    val currentBackStack by navController.currentBackStackEntryAsState() //me recoge donde me ubico
    val currentRoute = currentBackStack?.destination?.route //ruta completa de la pantalla donde estoy

    //para que pantalla se va a ver cada barra
    val showTopBar = currentRoute == Screens.Register.route || currentRoute == Screens.Tasks.route
    val showBottomBar = currentRoute == Screens.Tasks.route

    LaunchedEffect(authState) {
        // Cuando el estado de autenticación cambie, verificar el homeId
        if (authState is LoginViewModel.AuthState.Authenticated) {
            homeViewModel.loadUser()

            // Aquí observamos el estado del home y navegamos según corresponda
            when (val state = homeViewModel.uiState.value) {
                is HomeViewModel.UiState.HasHome -> {
                    // Si tiene un hogar, redirigir a Tasks
                    navController.navigate(Screens.Tasks.route) {
                        // Eliminamos Login de la pila de navegación
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                }
                is HomeViewModel.UiState.NotHome -> {
                    // Si no tiene hogar, redirigir a Home
                    navController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }



    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentRoute) { //texto del titulo de la pagina
                                Screens.Register.route -> ""
                                Screens.Tasks.route -> stringResource(R.string.tasks)
                                else -> ""
                            }
                        )
                    },
                    navigationIcon = {
                        if (currentRoute == Screens.Register.route) { //en el registro debe aparecer la flecha hacia atras
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.atras))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(id = R.color.greyBackground),
                        titleContentColor = colorResource(id = R.color.black),
                        navigationIconContentColor = colorResource(id = R.color.black),
                        actionIconContentColor = colorResource(id = R.color.white)
                    ),
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screens.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screens.Login.route) {
                LoginScreen(viewModel = loginViewModel, navController)
            }
            composable(Screens.Register.route) {
                RegisterScreen(viewModel = RegisterViewModel(navController))
            }
            composable(Screens.Tasks.route) {
                TasksScreen(viewModel = TasksViewModel(), navController)
            }
            composable(Screens.Home.route) {
                HomeScreen(viewModel = homeViewModel, navController)
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = stringResource(R.string.tasks)) },
            label = { Text(stringResource(R.string.tasks)) },
            selected = false, // Lo mejoramos luego
            onClick = { navController.navigate(Screens.Tasks.route) }
        )
    }
}
/*
@Composable
fun TopBar(navcontroller: NavController)
{
    Icon(imageVector = Icons.Default.ArrowBack,
        contentDescription = "Back",
        modifier = Modifier
            .clickable{
                navcontroller.popBackStack()
            }
            .padding(top = 40.dp, start = 10.dp)
            .size(34.dp)
    )

}*/