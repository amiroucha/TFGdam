package com.example.tfg_1.navigation

import androidx.compose.foundation.*
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewmodel.compose.viewModel
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tfg_1.R
import com.example.tfg_1.ui.ui.*
import com.example.tfg_1.viewModel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.tfg_1.ui.ui.AvatarSheet
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationWrapper(themeViewModel: ThemeViewModel, openChat: Boolean = false) {
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
    val showTopBar = currentRoute != Screens.Home.route
    val showBottomBar = currentRoute == Screens.Tasks.route || currentRoute == Screens.Expenses.route || currentRoute == Screens.Chat.route

    //filtro de tareas por usesr
    var tasksViewModel by remember { mutableStateOf<TasksViewModel?>(null) }

    //filtro de expenses por fecha
    var expensesViewModel by remember { mutableStateOf<ExpensesViewModel?>(null) }

    //drawer
    //pantallas en las que no quiero que se vea el drawer
    val drawerEnabled = currentRoute != Screens.Login.route &&  currentRoute != Screens.Register.route && currentRoute != Screens.Home.route// Puedes agregar más rutas aquí

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    //filtro de busqueda para chat
    val chatViewModel: ChatViewModel = viewModel()

    var showSearchBar by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }


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
    //notificaciones
    LaunchedEffect(openChat) {
        if (openChat) {
            navController.navigate(Screens.Chat.route) {
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
                            if (currentRoute == Screens.Chat.route && showSearchBar) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    TextField(
                                        value = searchText,
                                        onValueChange = { newText ->
                                            searchText = newText
                                            chatViewModel.updateSearchQuery(newText)
                                        },
                                        placeholder = { Text("Buscar mensaje...") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(70.dp)
                                            .size(15.dp),
                                        singleLine = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            cursorColor = MaterialTheme.colorScheme.primary,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        ),
                                        textStyle = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }  else {
                                Text(
                                    text = when (currentRoute) { //texto del titulo de la pagina
                                        Screens.Tasks.route -> stringResource(R.string.tasks)
                                        Screens.Settings.route -> stringResource(id = R.string.settings)
                                        Screens.Expenses.route -> stringResource(id = R.string.gastos)
                                        Screens.Chat.route -> stringResource(R.string.chat_familiar)
                                        else -> ""
                                    }
                                )
                            }
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
                            //filtro de usuario en tasks y expenses
                            if (currentRoute == Screens.Tasks.route && tasksViewModel != null)
                            {
                                var expanded by remember { mutableStateOf(false) }
                                val tasksviewModel = tasksViewModel!!
                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.Person, contentDescription = "Filtrar por usuario")
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
                                                tasksviewModel.modificaUsuarioFiltrado(null)
                                                expanded = false
                                            }
                                        )
                                        tasksviewModel.usuarios.forEach { usuario ->
                                            DropdownMenuItem(
                                                text = { Text(text = usuario,
                                                    color= MaterialTheme.colorScheme.onBackground) },
                                                onClick = {
                                                    tasksviewModel.modificaUsuarioFiltrado(usuario)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            //filtro usuario pantalla de gastos/expenses
                            if (currentRoute == Screens.Expenses.route && expensesViewModel != null) {
                                var expanded by remember { mutableStateOf(false) }
                                val viewModelExpenses = expensesViewModel!!
                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.Person,
                                            contentDescription = "Filtrar por usuario")
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
                                                viewModelExpenses.modificaUsuarioFiltrado(null)
                                                expanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(
                                                text="Hogar",
                                                color= MaterialTheme.colorScheme.onBackground
                                            ) },
                                            onClick = {
                                                viewModelExpenses.modificaUsuarioFiltrado("Hogar")
                                                expanded = false
                                            }
                                        )
                                        viewModelExpenses.usuarios.forEach { usuario ->
                                            //por cada usuario me enseña su nombre
                                            DropdownMenuItem(
                                                text = { Text(text = usuario,
                                                    color= MaterialTheme.colorScheme.onBackground) },
                                                onClick = {
                                                    viewModelExpenses.modificaUsuarioFiltrado(usuario)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (currentRoute == Screens.Chat.route) {
                                if (showSearchBar) {
                                    IconButton(onClick = {
                                        searchText = ""
                                        showSearchBar = false
                                        chatViewModel.updateSearchQuery("")

                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda")
                                    }
                                } else {
                                    IconButton(onClick = {
                                        showSearchBar = true
                                    }) {
                                        Icon(Icons.Default.Search, contentDescription = "Buscar")
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
                    BottomBar(navController, currentRoute)
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
                    HomeScreen(viewModel = homeViewModel)
                }
                composable(Screens.Settings.route){
                    SettingsScreen(navController,themeViewModel)
                }
                composable(Screens.Expenses.route){ backStackEntry ->
                    expensesViewModel = viewModel(backStackEntry)
                    ExpensesScreen()
                }
                composable(Screens.Chat.route){
                    ChatScreen(viewModel = chatViewModel,
                        searchText = searchText)

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
fun BottomBar(navController: NavHostController, currentRoute :String?) {
    NavigationBar {
        //tareas
        NavigationBarItem(
            icon = { Icon(Icons.Default.List,
                contentDescription = stringResource(R.string.tasks)) },
            label = { Text(stringResource(R.string.tasks)) },
            selected = currentRoute == Screens.Tasks.route, //le da color
            onClick = { navController.navigate(Screens.Tasks.route) },
        )
        //gastos
        NavigationBarItem(
            icon = { Icon(Icons.Default.AttachMoney,
                contentDescription = stringResource(R.string.gastos)
            ) },
            label = { Text(stringResource(R.string.gastos)) },
            selected = currentRoute == Screens.Expenses.route,
            onClick = {
                navController.navigate(Screens.Expenses.route)
            },
        )
        //chat
        NavigationBarItem(
            icon = { Icon(Icons.Default.Chat,
                contentDescription = stringResource(R.string.chat)
            ) },
            label = { Text( stringResource(R.string.chat)) },
            selected = currentRoute == Screens.Chat.route,
            onClick = {
                navController.navigate(Screens.Chat.route)
            },
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
            Row(
                modifier = Modifier.padding(8.dp)
            ) {
                // Encabezado con el nombre del usuario
                Text(
                    text = userName,
                    fontSize = 30.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp, top = 20.dp, start = 6.dp, end=20.dp)
                )
                //IMAGEN DE PERFIL-----------------------------------

                FotoPerfil()
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
private fun FotoPerfil() {
    val avatarViewModel: AvatarViewModel = viewModel()

    var showAvatarPicker by remember { mutableStateOf(false) }
    val selectedAvatar = avatarViewModel.selectedAvatar

    if (showAvatarPicker) {
        AvatarSheet(
            onAvatarSelected = { imageUrl ->
                avatarViewModel.guardarAvatar(imageUrl)
                showAvatarPicker = false
            },
            onDismiss = { showAvatarPicker = false }
        )
    }

    if (selectedAvatar != null) {
        AsyncImage(
            model = selectedAvatar,
            contentDescription = "Selected Avatar",
            modifier = Modifier
                .padding(5.dp)
                .size(85.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = colorResource(id = R.color.black),
                    shape = CircleShape
                )
                .clickable { showAvatarPicker = true }
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.perfil_imagen),
            contentDescription = "Default Avatar",
            modifier = Modifier
                .padding(5.dp)
                .size(85.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = colorResource(id = R.color.black),
                    shape = CircleShape
                )
                .clickable { showAvatarPicker = true }
        )
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

//filtro de fecha que quiero quitar

/*
//filtro para lista de gastos fecha en x rango
@Composable
fun DateFilterDialog(
    initialStartDate: Date?,
    initialEndDate: Date?,
    onDismiss: () -> Unit,
    onFilter: (startDate: Date?, endDate: Date?) -> Unit
) {
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filtrar_por_fecha)) },
        text = {
            Column {
                //fecha inicio------------------------------------
                Text(stringResource(R.string.fecha_inicio))
                DatePickerField(
                    selectedDate = startDate,
                    onDateSelected = { startDate = it })

                Spacer(Modifier.height(16.dp))

                //fecha fin------------------------------------
                Text(stringResource(R.string.fecha_fin))
                DatePickerField(
                    selectedDate = endDate,
                    onDateSelected = { endDate = it })


                Spacer(Modifier.height(24.dp))

                // botón para quitar el filtro------------------------------
                TextButton(
                    onClick = {
                        onFilter(null, null) // Quita el filtro
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.align(Alignment.Start),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text =stringResource(R.string.quitar_filtros))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onFilter(startDate, endDate) }) {
                Text(stringResource(R.string.aceptar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(selectedDate: Date?, onDateSelected: (Date?) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showPicker by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    if (selectedDate != null) calendar.time = selectedDate

    OutlinedButton(onClick = { showPicker = true },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), // fondo claro
            contentColor = MaterialTheme.colorScheme.primary // color del texto e iconos
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(text = selectedDate?.let { dateFormat.format(it) } ?: stringResource(R.string.seleccionar_fecha))
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = calendar.timeInMillis)

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val pickedMillis = datePickerState.selectedDateMillis
                    if (pickedMillis != null) {
                        onDateSelected(Date(pickedMillis))
                    }
                    showPicker = false
                }) {
                    Text(stringResource(R.string.aceptar))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPicker = false
                }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

}*/










/*
                        var showFilterDialog by remember { mutableStateOf(false) }
                        val expensesVM = expensesViewModel!!
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtrar por fecha")
                        }
                        if (showFilterDialog) {
                            // Aquí muestras un diálogo para elegir fechas
                            DateFilterDialog(
                                initialStartDate = expensesVM.fechaInicio.value,
                                initialEndDate = expensesVM.fechaFin.value,
                                onDismiss = { showFilterDialog = false },
                                onFilter = { start, end ->
                                    expensesVM.setFechaInicio(start)
                                    expensesVM.setFechaFin(end)
                                    showFilterDialog = false
                                }
                            )
                        }*/


