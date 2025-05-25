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
import androidx.compose.material.icons.filled.*
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
import com.example.tfg_1.model.TasksModel
import com.example.tfg_1.R
import com.example.tfg_1.viewModel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.*

@Preview(showBackground = true)
@Composable
fun TasksScreenPreview() {
    val viewModel = TasksViewModel()
    TasksScreen(viewModel = viewModel)
}

@Composable
fun TasksScreen(viewModel: TasksViewModel) {
    Box(
        Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.greyBackground))
    ) {
        TasksBody(
            Modifier
                .align(Alignment.Center)
                .padding(10.dp), viewModel )

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
            )


        }
    }

}


@Composable
fun TasksBody (modifier: Modifier, viewModel: TasksViewModel) {

    Column{
        TabsPag(viewModel) // Componente para las pestañas
        Spacer(modifier = modifier.height(10.dp))
    }

}

@Composable
fun TabsPag(viewModel: TasksViewModel) {
    val selectedTab = remember { mutableIntStateOf(0) }

    val tabs = listOf(
        TabData(stringResource(R.string.pendientes), Icons.Filled.List),
        TabData(stringResource(R.string.completadas), Icons.Filled.Check),
    )

    Column {
        TabRow(selectedTabIndex = selectedTab.intValue) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab.intValue == index,
                    onClick = { selectedTab.intValue = index },
                    text = { Text(text = tab.title) },
                    icon = { Icon(
                        imageVector = tab.icon,
                        contentDescription = null // Provide a content description if needed
                    )},

                )
            }
        }

        // Contenido para cada tab
        when (selectedTab.intValue) {
            0 -> Pendientes(viewModel)
            1 -> Completadas(viewModel)
        }
    }
}


//Creamos una data class para el texto y el titulo del Tab
data class TabData(val title: String, val icon: ImageVector)

//caja de item, cada tarea creada
@Composable
fun TareaItem(tarea: TasksModel,
              modificarCompletada: (TasksModel) -> Unit,
              eliminarTarea: (TasksModel) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showToggleCompletedDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.estasSeguro)) },
            text = { Text(stringResource(R.string.confirmarEliminarTarea)) },
            confirmButton = {
                TextButton(onClick = {
                    eliminarTarea(tarea)
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.eliminar_tarea))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }

    if (showToggleCompletedDialog) {
        val mensaje = if (!tarea.completada)
            stringResource(R.string.confirmarCompletarTarea)
        else
            stringResource(R.string.confirmarMarcarComoPendiente)

        AlertDialog(
            onDismissRequest = { showToggleCompletedDialog = false },
            title = { Text(stringResource(R.string.estasSeguro)) },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = {
                    modificarCompletada(tarea)
                    showToggleCompletedDialog = false
                }) {
                    Text(stringResource(R.string.aceptar))
                }
            },
            dismissButton = {
                TextButton(onClick = { showToggleCompletedDialog = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
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
            // Cuando cambia el checkbox, alterna completada, pregunta ¿seguro?
            onCheckedChange = { showToggleCompletedDialog = true },
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
        IconButton(onClick = { showDeleteDialog = true  }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.eliminar_tarea),
                tint = Color.Red
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
    usuarios: List<String>
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
fun Pendientes(viewModel: TasksViewModel) {
    val tareasPendientes by remember {
        derivedStateOf { viewModel.tareasPendientes() }
    }

    LazyColumn {
        items(tareasPendientes) { tarea ->
            TareaItem(
                tarea = tarea,
                modificarCompletada = { viewModel.comprobarEstadoTarea(it) },
                eliminarTarea = { viewModel.eliminarTarea(it) }
            )
        }
    }
}


@Composable
fun Completadas(viewModel: TasksViewModel) {
    val tareasCompletadas by remember {
        derivedStateOf { viewModel.tareasCompletadas() }
    }

    LazyColumn {
        items(tareasCompletadas) { tarea ->
            TareaItem(
                tarea = tarea,
                modificarCompletada = { viewModel.comprobarEstadoTarea(it) },
                eliminarTarea = { viewModel.eliminarTarea(it) }
            )
        }
    }
}