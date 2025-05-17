@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tfg_1.ui.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tfg_1.model.TasksModel
import com.example.tfg_1.R
import com.example.tfg_1.viewModel.TasksViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Preview(showBackground = true)
@Composable
fun TasksScreenPreview() {
    val navController = rememberNavController()
    val viewModel = TasksViewModel()
    TasksScreen(viewModel = viewModel, navController)
}

@Composable
fun TasksScreen(viewModel: TasksViewModel, navcontroller : NavController) {
    Box(
        Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.greyBackground))
    ) {
        TasksBody(
            Modifier
                .align(Alignment.Center)
                .padding(10.dp), viewModel ,navcontroller )

        var showDialog by remember { mutableStateOf(false) }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd) // Lo coloca en la esquina inferior derecha
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, stringResource(R.string.nuevaTarea))
        }

        if (showDialog){
            NuevaTareaFormulario(
                dismiss = { showDialog = false },
                save = {titulo, fecha, asignadoA ->
                    viewModel.agregarTarea(titulo, fecha, asignadoA)
                    showDialog = false
                },
                usuarios = viewModel.usuarios,
                viewModel = viewModel,
            )


        }
    }

}


@Composable
fun TasksBody (modifier: Modifier, viewModel: TasksViewModel, navcontroller : NavController) {

    Column{
        TabsPag(viewModel) // Componente para las pestañas
        Spacer(modifier = modifier.height(10.dp))
    }

}

@Composable
fun TabsPag(viewModel: TasksViewModel) {
    val selectedTab = remember { mutableStateOf(0) }

    val tabs = listOf(
        TabData(stringResource(R.string.pendientes), Icons.Filled.List),
        TabData(stringResource(R.string.completadas), Icons.Filled.Check),
    )

    Column {
        TabRow(selectedTabIndex = selectedTab.value) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab.value == index,
                    onClick = { selectedTab.value = index },
                    text = { Text(text = tab.title) },
                    icon = { Icon(
                        imageVector = tab.icon,
                        contentDescription = null // Provide a content description if needed
                    )},
                    //unselectedContentColor = MaterialTheme.colorScheme.background

                )
            }
        }

        // Contenido para cada tab
        when (selectedTab.value) {
            0 -> pendientes(viewModel)
            1 -> completadas(viewModel)
        }
    }
}


//Creamos una data class para el texto y el titulo del Tab
data class TabData(val title: String, val icon: ImageVector)

//caja de item
@Composable
fun tareaItem(tarea: TasksModel, modificarCompletada: (TasksModel) -> Unit) {
    // Contenedor de la tarea con padding y márgenes
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // Añadimos padding para toda la caja de la tarea
            .background(colorResource(id = R.color.white), RoundedCornerShape(8.dp)) // Fondo con bordes redondeados
            .clickable { modificarCompletada(tarea) } // Al hacer clic cambia el estado de la tarea
    ) {
        // Checkbox para marcar la tarea como completada o no
        Checkbox(
            checked = tarea.completada,
            onCheckedChange = { modificarCompletada(tarea) }, // Cuando cambia el checkbox, alterna completada
            modifier = Modifier.padding(end = 16.dp) // Separar el checkbox del texto
        )

        // Columna para colocar el título y la fecha de la tarea
        Column(
            modifier = Modifier
                .weight(1f) // Esto asegura que el texto ocupe el espacio restante disponible
                .padding(end = 8.dp)
        ) {
            // Título de la tarea
            Text(
                text = tarea.titulo,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1, // Limitar a una línea,
                modifier = Modifier.padding(8.dp),
                overflow = TextOverflow.Ellipsis, // si es muy largo ...
                color = colorResource(id = R.color.black)
            )

            // Fecha de la tarea
            Text(
                text = stringResource(R.string.fechaTarea, tarea.fecha),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(id = R.color.black) ,
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = stringResource(R.string.asignado_a, tarea.asignadoA),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(id = R.color.black), // Estilo de texto más tenue para el asignado
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}


//para el dialog que aparece y rellenar datos
//le paso 2 funciones: cuando se cancela - para los datos
@Composable
fun NuevaTareaFormulario(
    dismiss: () -> Unit,
    save: (String, String, String) -> Unit,
    usuarios: List<String>,
    viewModel: TasksViewModel
) {
    val context = LocalContext.current
    var titulo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var asignadoA by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                fecha = dateFormatter.format(calendar.time)//asigno fecha a la variable
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
    //la alerta
    AlertDialog(
        onDismissRequest = { dismiss() },
        title = { Text(stringResource(R.string.nuevaTarea)) },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text(stringResource(R.string.titulo)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                //fecha

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                ) {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.fechaMax)) },
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = stringResource(R.string.seleccionaFecha)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = asignadoA,
                        onValueChange = {},

                        readOnly = true,
                        label = { Text(stringResource(R.string.asignadoA_)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black
                    )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        usuarios.forEach { usuario ->
                            DropdownMenuItem(
                                text = { Text(usuario) },
                                onClick = {
                                    asignadoA = usuario
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (titulo.isNotBlank() && fecha.isNotBlank() && asignadoA.isNotBlank()) {
                        save(titulo, fecha, asignadoA)
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.completaTodosCampos),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text(stringResource(R.string.guardar))
            }
        },
        dismissButton = {
            TextButton(onClick = { dismiss() }) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}


@Composable
fun pendientes(viewModel: TasksViewModel) {
    val tareasPendientes by remember {
        derivedStateOf { viewModel.tareasPendientes() }
    }

    LazyColumn {
        items(tareasPendientes) { tarea ->
            tareaItem(tarea = tarea) {
                viewModel.comprobarEstadoTarea(tarea)
            }
        }
    }
}


@Composable
fun completadas(viewModel: TasksViewModel) {
    val tareasCompletadas by remember {
        derivedStateOf { viewModel.tareasCompletadas() }
    }

    LazyColumn {
        items(tareasCompletadas) { tarea ->
            tareaItem(tarea = tarea) {
                viewModel.comprobarEstadoTarea(tarea)
            }
        }
    }
}
/*
@Composable
fun TasksScreenEntry(navController: NavController) {
    var viewModel by remember { mutableStateOf<TasksViewModel?>(null) }

    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("usuarios")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val homeId = document.getString("homeId")
                    if (!homeId.isNullOrEmpty()) {
                        viewModel = TasksViewModel(homeId)
                    }
                }
        }
    }

    viewModel?.let {
        TasksScreen(viewModel = it, navcontroller = navController)
    } ?: run {
        // puedes mostrar un loader o mensaje de error
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}*/
