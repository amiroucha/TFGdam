@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tfg_1.ui.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tfg_1.Model.Tarea
import com.example.tfg_1.R
import com.example.tfg_1.viewModel.TasksViewModel

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
            //.verticalScroll(rememberScrollState()) da problems con el lazy
            .background(color = colorResource(id = R.color.greyBackground))
    ) {
        TasksBody(Modifier.align(Alignment.Center).padding(10.dp), viewModel ,navcontroller )

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
                }
            )


        }
    }

}


@Composable
fun TasksBody (modifier: Modifier, viewModel: TasksViewModel, navcontroller : NavController) {

    Column{
        TabsPag(viewModel) // Componente para las pestañas
        Spacer(modifier = Modifier.height(10.dp))
        //nuevaTarea(viewModel)
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
                    )}
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
fun tareaItem(tarea: Tarea, modificarCompletada: (Tarea) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(color = colorResource(id = R.color.white))
            .clickable { modificarCompletada(tarea) } // si toca cambia el estado de la tarea
    ) {
        // Checkbox para marcar la tarea como completada o no
        Checkbox(
            checked = tarea.completada,
            onCheckedChange = { modificarCompletada(tarea) }
        )

        // Texto de la tarea
        Spacer(modifier = Modifier
            .fillMaxWidth())
        Text(
            text = tarea.titulo,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f) // Esto hace que el texto ocupe el espacio disponible
        )

        // Fecha de la tarea
        Spacer(modifier = Modifier
            .fillMaxWidth())
        Text(
            text = tarea.fecha,
            style = MaterialTheme.typography.bodyMedium
        )
        //para quien es
        Spacer(modifier = Modifier
            .fillMaxWidth())
        Text(
            text = tarea.asignadoA,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

//para el dialog que aparece y rellenar datos
//le paso 2 funciones: cuando se cancela - para los datos
@Composable
fun NuevaTareaFormulario(
    dismiss: ()-> Unit,
    save: (String, String, String) -> Unit,
){
    val context = LocalContext.current
    var titulo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var asignadoA by remember { mutableStateOf("")}

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
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text(stringResource(R.string.fechaMax)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = asignadoA,
                    onValueChange = { asignadoA = it },
                    label = { Text(stringResource(R.string.asignadoA_)) },
                    singleLine = true
                )
            }

        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (titulo.isNotBlank() && fecha.isNotBlank() && asignadoA.isNotBlank()) {
                        save(titulo, fecha, asignadoA)
                    } else {
                        //si falta algún camppo
                        Toast.makeText(
                            context,
                            context.getString(R.string.completaTodosCampos),
                            Toast.LENGTH_SHORT).show()
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
    val tareasPendientes = remember { viewModel.tareasPendientes() }

    LazyColumn {
        items(tareasPendientes) { tarea ->
            tareaItem(tarea = tarea) { tarea ->
                viewModel.comprobarEstadoTarea(tarea)
            }
        }
    }
}

@Composable
fun completadas(viewModel: TasksViewModel) {
    val tareasCompletadas = remember { viewModel.tareasCompletadas() }

    LazyColumn {
        items(tareasCompletadas) { tarea ->
            tareaItem(tarea = tarea) { tarea ->
                viewModel.comprobarEstadoTarea(tarea)
            }
        }
    }
}

