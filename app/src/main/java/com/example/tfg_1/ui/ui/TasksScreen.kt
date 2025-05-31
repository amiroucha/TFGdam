@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tfg_1.ui.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
    val loading by viewModel.loading
    Box(
        Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)

    ) {

        if(loading){
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
        else {
            TasksBody(
                 viewModel )

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
}


@Composable
fun TasksBody ( viewModel: TasksViewModel) {

    Column{
        TabsPag(viewModel) // Componente para las pestañas
    }

}

@Composable
fun TabsPag(viewModel: TasksViewModel) {
    val context = LocalContext.current
    val selectedTab = remember { mutableIntStateOf(0) }

    val tabs = listOf(
        TabData(stringResource(R.string.pendientes), Icons.Filled.List),
        TabData(stringResource(R.string.completadas), Icons.Filled.Check),
    )
    val selectedColor = Color.White
    val unselectedColor = Color.Gray

    Column {
        TabRow(
            selectedTabIndex = selectedTab.intValue,
            contentColor = selectedColor, //color del indicador
            //subrayar en la que estoy
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab.intValue]),
                    color = selectedColor,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTab.intValue == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab.intValue = index },
                    text = {
                        Text(
                            text = tab.title,
                            color = if (isSelected) selectedColor else unselectedColor
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = if (isSelected) selectedColor else unselectedColor
                        )
                    },
                    modifier = Modifier
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = Color.Gray,


                )

            }
        }
        //filtro del user
        FiltroUsuarios(viewModel)
        // escuchar eventos y mostrar Toast
        LaunchedEffect(Unit) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    TasksViewModel.UiEvent.Added -> {
                        Toast.makeText(context, context.getString(R.string.tarea_guardada), Toast.LENGTH_SHORT).show()
                    }
                    is TasksViewModel.UiEvent.Error -> {
                        Toast.makeText(context, event.msg, Toast.LENGTH_LONG).show()
                    }
                    is TasksViewModel.UiEvent.Emit -> {
                        Toast.makeText(context, event.msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        // Contenido para cada tab
        when (selectedTab.intValue) {
            0 -> Pendientes(viewModel,context )
            1 -> Completadas(viewModel, context)
        }
    }
}


//Creamos una data class para el texto y el titulo del Tab
data class TabData(val title: String, val icon: ImageVector)

//caja de item, cada tarea creada
@Composable
fun TareaItem(tarea: TasksModel,
              usuarios: List<String>,
              modificarCompletada: (TasksModel) -> Unit,
              eliminarTarea: (TasksModel) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showToggleCompletedDialog by remember { mutableStateOf(false) }
    val asignadoActivo = usuarios.contains(tarea.asignadoA)

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
    // Contenedor de la tarea con padding y márgenes background(color = MaterialTheme.colorScheme.background)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
            //.clickable { modificarCompletada(tarea) } // Al hacer clic cambia el estado de la tarea
        ) {
            // Checkbox para marcar la tarea como completada o no
            Checkbox(
                checked = tarea.completada,
                // Cuando cambia el checkbox, alterna completada, pregunta ¿seguro?
                onCheckedChange = { showToggleCompletedDialog = true },
                modifier = Modifier
                    .padding(end = 16.dp) // Separar el checkbox del texto
                    .clickable { modificarCompletada(tarea) }
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
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Fecha de la tarea
                Text(
                    text = stringResource(R.string.fechaTarea, tarea.fecha),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
                Text(
                    text =  buildAnnotatedString {
                        // agregar texto base con stringResource
                        append(stringResource(R.string.asignado_a, tarea.asignadoA))
                        if (!asignadoActivo) {
                            append(" - ")
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                                append(stringResource(R.string.dado_de_baja))
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
            IconButton(onClick = { showDeleteDialog = true  }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.eliminar_tarea),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


//para el dialog que aparece y rellenar datos
//le paso 2 funciones: cuando se cancela - para los datos
@OptIn(ExperimentalMaterial3Api::class)
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
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
fun FiltroUsuarios(viewModel: TasksViewModel) {
    var expanded by remember { mutableStateOf(false) }

    // Usuario actualmente filtrado
    val usuarioSeleccionado = viewModel.usuarios.find { it == viewModel.usuarioFiltrado } ?: stringResource(R.string.todos)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)//margen
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(2.dp)
            )
            .padding(14.dp)//padidng interno
            .clickable { expanded = true }, // clicable toda la fila
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icono con fondo circular
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(15.dp))

        // Texto Usuario filtrado: Nombre
        Text(
            text = "${stringResource(R.string.usuario_filtrado)}:      $usuarioSeleccionado",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.weight(1f))

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.todos)) },
                onClick = {
                    viewModel.modificaUsuarioFiltrado(null)
                    expanded = false
                }
            )
            viewModel.usuarios.forEach { usuario ->
                DropdownMenuItem(
                    text = { Text(usuario) },
                    onClick = {
                        viewModel.modificaUsuarioFiltrado(usuario)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun Pendientes(viewModel: TasksViewModel, context: Context) {
    val tareasPendientes by remember {
        derivedStateOf { viewModel.tareasPendientes() }
    }
    val usuarios = viewModel.usuarios

    if (tareasPendientes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_hay_tareas_pendientes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn {
            items(tareasPendientes) { tarea ->
                TareaItem(
                    tarea = tarea,
                    usuarios = usuarios,
                    modificarCompletada = { viewModel.comprobarEstadoTarea(it,context) },
                    eliminarTarea = { viewModel.eliminarTarea(it, context) }
                )
            }
        }
    }
}


@Composable
fun Completadas(viewModel: TasksViewModel,context:Context) {
    val tareasCompletadas by remember {
        derivedStateOf { viewModel.tareasCompletadas() }
    }
    val usuarios = viewModel.usuarios
    if (tareasCompletadas.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_hay_tareas_completadas),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn {
            items(tareasCompletadas) { tarea ->
                TareaItem(
                    tarea = tarea,
                    usuarios = usuarios,
                    modificarCompletada = { viewModel.comprobarEstadoTarea(it,context, deCompletadas = true) },
                    eliminarTarea = { viewModel.eliminarTarea(it, context, deCompletadas = true) }
                )
            }
        }
    }
}