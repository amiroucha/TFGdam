package com.example.tfg_1.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg_1.R
import com.example.tfg_1.ui.ui.*
import com.example.tfg_1.viewModel.HomeViewModel
import com.example.tfg_1.viewModel.LoginViewModel
import com.example.tfg_1.viewModel.RegisterViewModel
import com.example.tfg_1.viewModel.TasksViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

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

    //drawer
    //pantallas en las que no quiero que se vea el drawer
    val drawerEnabled = currentRoute != Screens.Login.route ||  currentRoute != Screens.Register.route || currentRoute != Screens.Home.route// Puedes agregar más rutas aquí

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    LaunchedEffect(authState) {
        // Cuando el estado de autenticación cambie, verificar el homeId
        if (authState is LoginViewModel.AuthState.Authenticated) {
            homeViewModel.loadUser()

            // Aquí observamos el estado del home y navegamos según corresponda
            when (homeViewModel.uiState.value) {
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

    val contentScaffold: @Composable () -> Unit = {
        Scaffold(
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (currentRoute) { //texto del titulo de la pagina
                                    Screens.Register.route , Screens.Login.route -> stringResource(R.string.app)
                                    Screens.Tasks.route -> stringResource(R.string.tasks)
                                    else -> ""
                                }
                            )
                        },
                        navigationIcon = {
                            if (currentRoute == Screens.Register.route) { //en el registro debe aparecer la flecha hacia atras
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.Default.ArrowBack,
                                        contentDescription = stringResource(R.string.atras))
                                }
                            }else if (currentRoute == Screens.Tasks.route) {
                                IconButton(onClick = {
                                    scope.launch { drawerState.open() }
                                }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        },actions = {
                            if (currentRoute == Screens.Register.route || currentRoute == Screens.Login.route) { //texto del titulo de la pagina
                                Image(
                                    painter = painterResource(id= R.drawable.logotfg),
                                    contentDescription = "Hogar",
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                    // .border(7.dp, color = Color.Black)
                                )
                            }
                            if (currentRoute == Screens.Tasks.route) {
                                IconButton(onClick = {
                                    //loginViewModel.logout()
                                    navController.navigate(Screens.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
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
            },

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
                    //TasksScreen(viewModel = TasksViewModel(), navController)
                    TasksScreenEntry(navController)
                }
                composable(Screens.Home.route) {
                    HomeScreen(viewModel = homeViewModel, navController)
                }
            }
        }
    }

    if (drawerEnabled) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = { DrawerContent(homeViewModel) },
            scrimColor = colorResource(id = R.color.blue), // Fondo del Drawer
            gesturesEnabled = false // swipe desde el borde

        ) {
            contentScaffold()
        }
    }else
    {
        contentScaffold()
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
@Composable
fun DrawerContent(homeViewModel: HomeViewModel) {
    var userName by remember { mutableStateOf("") }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val drawerWidth = screenWidth * 0.8f

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("name") ?: ""
                }
        }
    }
    Column(
        modifier = Modifier
            .width(drawerWidth) // ocupa solo el 80 ancho de pantalla
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        // Encabezado con el nombre del usuario
        Text(
            text = "Hola, $userName",
            fontSize = 30.sp,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Lista de secciones
        Column(modifier = Modifier.padding(vertical = 8.dp)) {

        }
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