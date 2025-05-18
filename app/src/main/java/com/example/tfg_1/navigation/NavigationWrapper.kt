package com.example.tfg_1.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.navigation.NavController
import com.example.tfg_1.R
import com.example.tfg_1.ui.ui.*
import com.example.tfg_1.viewModel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationWrapper(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()

    val loginViewModel: LoginViewModel = viewModel()
    val authState by loginViewModel.authState.collectAsState()

    val homeViewModel: HomeViewModel = viewModel()
    val homeUiState by homeViewModel.uiState.collectAsState()

    val settingsViewModel: SettingsViewModel = viewModel()
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    val currentBackStack by navController.currentBackStackEntryAsState() //me recoge donde me ubico
    val currentRoute = currentBackStack?.destination?.route //ruta completa de la pantalla donde estoy

    //para que pantalla se va a ver cada barra
    val showTopBar = currentRoute != Screens.Home.route //|| currentRoute == Screens.Tasks.route
    val showBottomBar = currentRoute == Screens.Tasks.route

    //filtro de tareas por s¡usesr
    var tasksViewModel by remember { mutableStateOf<TasksViewModel?>(null) }

    //drawer
    //pantallas en las que no quiero que se vea el drawer
    val drawerEnabled = currentRoute != Screens.Login.route &&  currentRoute != Screens.Register.route && currentRoute != Screens.Home.route// Puedes agregar más rutas aquí

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()



    // Observa los cambios en authState para manejo de navegación
    LaunchedEffect(authState) {
        if (authState is LoginViewModel.AuthState.Unauthenticated) {
            navController.navigate(Screens.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(homeUiState, settingsUiState) {
        when (homeUiState) {
            is HomeViewModel.UiState.HasHome -> {
               //navega solo cuando ha cambiado ya de hogar
                if (settingsUiState !is SettingsViewModel.SettingsUiState.LeftHome &&
                    settingsUiState !is SettingsViewModel.SettingsUiState.Loading) {

                    navController.navigate(Screens.Tasks.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is HomeViewModel.UiState.NotHome -> {
                navController.navigate(Screens.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    // Observa cambios en settingsUiState para sincronizar con HomeViewModel
    LaunchedEffect(settingsUiState) {
        if (settingsUiState is SettingsViewModel.SettingsUiState.LeftHome) {
            //refresca HomeViewModel
            homeViewModel.loadUser()

            // pantalla Home
            navController.navigate(Screens.Home.route) {
                popUpTo(0) { inclusive = true }
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
                                    Screens.Tasks.route -> stringResource(R.string.tasks)
                                    Screens.Settings.route -> stringResource(id = R.string.settings)
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
                            }else if (currentRoute != Screens.Home.route && currentRoute != Screens.Login.route && currentRoute != Screens.Register.route) {
                                IconButton(onClick = {
                                    scope.launch { drawerState.open() }
                                }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        },actions = {
                            if (currentRoute == Screens.Tasks.route && tasksViewModel != null) {
                                var expanded by remember { mutableStateOf(false) }
                                val viewModel = tasksViewModel!!
                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar por usuario")
                                    }

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(
                                                text= stringResource(R.string.todos),
                                                color= MaterialTheme.colorScheme.onBackground
                                                ) },
                                            onClick = {
                                                viewModel.modificaUsuarioFiltrado(null)
                                                expanded = false
                                            }
                                        )
                                        viewModel.usuarios.forEach { usuario ->
                                            DropdownMenuItem(
                                                text = { Text(text = usuario,
                                                    color= MaterialTheme.colorScheme.onBackground) },
                                                onClick = {
                                                    viewModel.modificaUsuarioFiltrado(usuario)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            //logo de mi app
                                Image(
                                    painter = painterResource(id= R.drawable.logotfg),
                                    contentDescription = "Hogar",
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .size(50.dp)
                                        .clip(CircleShape)
                                    // .border(7.dp, color = Color.Black)
                                )

                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colorResource(id = R.color.greyBackground),
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
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
                startDestination = Screens.Splash.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screens.Splash.route) {
                    ScreenInitialize(navController = navController, loginViewModel = loginViewModel)
                }
                composable(Screens.Login.route) {
                    LoginScreen(viewModel = loginViewModel, navController)
                }
                composable(Screens.Register.route) {
                    RegisterScreen(viewModel = RegisterViewModel(navController))
                }
                composable(Screens.Tasks.route) { backStackEntry ->
                    tasksViewModel = viewModel(backStackEntry)
                    TasksScreen(viewModel = tasksViewModel!!, navController)
                }
                composable(Screens.Home.route) {
                    HomeScreen(viewModel = homeViewModel, navController)
                }
                composable(Screens.Settings.route){
                    SettingsScreen(navController,themeViewModel)
                }
            }
        }
    }
//Veo que paginas van a ver el drawer
    if (drawerEnabled) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = { DrawerContent(navController, loginViewModel,drawerState, scope) },
            scrimColor = MaterialTheme.colorScheme.surface, // Fondo del Drawer
            gesturesEnabled = true // swipe desde el borde
        ) {
            contentScaffold()
        }
    }else
    {
        contentScaffold()
    }
}

//mantener la sesion inciada
@Composable
fun ScreenInitialize(navController: NavController, loginViewModel: LoginViewModel) {
    val authState by loginViewModel.authState.collectAsState()

    // No navegamos aquí: NavigationWrapper lo hará.
    if (authState is LoginViewModel.AuthState.Unauthenticated) {
        // Usuario no logueado → ir a Login
        LaunchedEffect(Unit) {
            navController.navigate(Screens.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}


@Composable
fun BottomBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.List,
                contentDescription = stringResource(R.string.tasks)) },
            label = { Text(stringResource(R.string.tasks)) },
            selected = false, // Lo mejoramos luego
            onClick = { navController.navigate(Screens.Tasks.route) },
        )
    }
}
@Composable
fun DrawerContent(navController: NavController,
                  loginViewModel: LoginViewModel,
                  drawerState: DrawerState,
                  scope: CoroutineScope
) {
    var userName by remember { mutableStateOf("") }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val drawerWidth = screenWidth * 0.955f

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
            .width(drawerWidth) //ocupa solo el 80 ancho de pantalla
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Encabezado con el nombre del usuario
                Text(
                    text = userName,
                    fontSize = 30.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp, top = 20.dp)
                )

                Image(
                    painter = painterResource(id= R.drawable.logotfg),
                    contentDescription = stringResource(id = R.string.hogar),
                    modifier = Modifier
                        .padding(5.dp)
                        .size(50.dp)
                        .clip(CircleShape)
                )

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // caja que envuelve los items
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(stringResource(id = R.string.app),
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.hogar)) },
                    selected = false,
                    icon = { Icon(Icons.Outlined.Home, contentDescription = stringResource(id = R.string.hogar)) },
                    onClick = {
                        scope.launch {
                            navController.navigate(Screens.Tasks.route)
                            delay(500)
                            drawerState.close()
                        }
                    }
                )
                Spacer(Modifier.height(30.dp))
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.settings)) },
                    selected = false,
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = stringResource(id = R.string.settings)) },
                    onClick = {
                        scope.launch {
                            navController.navigate(Screens.Settings.route)
                            delay(500)
                            drawerState.close()
                        }
                    }
                )
                Spacer(Modifier.height(60.dp))
                LineaSeparacion()
                Spacer(Modifier.height(50.dp))
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.cerrarSesion)) },
                    selected = false,
                    icon = {
                        Icon(Icons.Default.ExitToApp, contentDescription = stringResource(id = R.string.cerrarSesion))
                    },
                    onClick = {
                        loginViewModel.logout()
                        navController.navigate(Screens.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun LineaSeparacion() {
    val lineColor = MaterialTheme.colorScheme.onSurface
    // línea
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = lineColor,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1f
        )
    }
}

