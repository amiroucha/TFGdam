package com.example.tfg_1.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg_1.R
import com.example.tfg_1.viewModel.ExpensesViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen() {

    val vm: ExpensesViewModel = viewModel()
    val gastos  by remember { derivedStateOf { vm.gastos } }
    val loading by remember { derivedStateOf { vm.loading } }
    val gastosFiltrados by remember { derivedStateOf { vm.gastosFiltrados } }


    var showDialog by remember { mutableStateOf(false) }
    // Snackbar  para avisar cuando fue guardado
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.uiEvent.collect { ev ->
            when (ev) {
                ExpensesViewModel.UiEvent.Added ->
                    snackbarHostState.showSnackbar("Gasto guardado")
                is ExpensesViewModel.UiEvent.Error ->
                    snackbarHostState.showSnackbar(ev.msg)
            }
        }
    }

    // estados del formulario ----------------------------------------------
    val categorias = listOf("comida", "agua", "luz", "limpieza", "aseo", "gas", "otra")
    var categoria    by remember { mutableStateOf(categorias.first()) }
    var otraCategoria by remember { mutableStateOf("") }
    var descripcion  by remember { mutableStateOf("") }
    var importeTxt   by remember { mutableStateOf("") }
    var fecha        by remember { mutableStateOf(Calendar.getInstance().time) }
    var showPicker   by remember { mutableStateOf(false) }
    val dateFmt      = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val datePickerState = rememberDatePickerState(fecha.time)
    val scope        = rememberCoroutineScope()




    /* ---------- guardar el gasto---------- */
    suspend fun save() {
        val imp = importeTxt.toDoubleOrNull() ?: 0.0
        val cat = if (categoria == "otra") otraCategoria else categoria
        if (cat.isBlank() || imp <= 0) {
            snackbarHostState.showSnackbar("Completa categoría e importe")
            return
        }
        if (vm.aniadirGastoVM(cat, descripcion, imp, fecha)) {
            categoria     = categorias.first()
            otraCategoria = ""
            descripcion   = ""
            importeTxt    = ""
        }
    }
    /* ---------- UI ---------- */
    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {


            //Alerta para añadir un nuevo gasto con x parametros
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch {
                                save()
                                showDialog = false
                            }
                        }) {
                            Text(stringResource(R.string.guardar))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text(stringResource(R.string.cancelar))
                        }
                    },
                    title = {
                        Text(stringResource(R.string.nuevo_gasto))
                    },
                    text = {
                        Column {
                            var expanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                                //categoria-----------------------------------------
                                TextField(
                                    value = categoria,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.categoria)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    categorias.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt) },
                                            onClick = {
                                                categoria = opt
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (categoria == "otra") {
                                OutlinedTextField(
                                    value = otraCategoria,
                                    onValueChange = { otraCategoria = it },
                                    label = { Text(stringResource(R.string.otra_categoria)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                )
                            }
                            //descripcion-----------------------------------------
                            OutlinedTextField(
                                value = descripcion,
                                onValueChange = { descripcion = it },
                                label = { Text(stringResource(R.string.descripcion)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )

                            //importe-----------------------------------------
                            OutlinedTextField(
                                value = importeTxt,
                                onValueChange = { importeTxt = it },
                                label = { Text("Importe (€)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )

                            //fecha-----------------------------------------
                            Text(
                                text = stringResource(R.string.selecciona_una_fecha),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            OutlinedButton(
                                onClick = { showPicker = true },
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(dateFmt.format(fecha))
                            }

                            if (showPicker) {
                                DatePickerDialog(
                                    onDismissRequest = { showPicker = false },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            datePickerState.selectedDateMillis?.let { millis ->
                                                fecha = Date(millis)
                                            }
                                            showPicker = false
                                        }) { Text(stringResource(R.string.aceptar)) }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.cancelar)) }
                                    }
                                ) {
                                    DatePicker(state = datePickerState)
                                }
                            }
                        }
                    }
                )
            }



            /* -------- Lista de gastos -------- */
            Text(
                text = stringResource(R.string.lista_de_gastos),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(Modifier.height(9.dp))
            //formo la lista de gastos
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 72.dp)
            ) {
                items(gastosFiltrados.sortedByDescending { it.fecha }) { g ->
                //colocar cada item de la lista en cajitas
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        )
                        {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "${g.categoria.replaceFirstChar { it.uppercase() }}: " +
                                            "${"%.2f".format(g.importe)}€  ·  ${dateFmt.format(g.fecha)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (g.descripcion.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = g.descripcion,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                            //icono para eliminar el gasto
                            IconButton(onClick = { vm.eliminarGasto(g) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.eliminar_gasto),
                                    tint = colorResource(id = R.color.red)
                                )
                            }
                        }
                    }
                }
            }
            //Botón para añadir gasto -----------------------------------------------
            Button(
                onClick = { showDialog = true },
                enabled = !loading,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.aniadir_gastoBT))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        // en caso de que esten llegando los datos
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

    }

}



/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionRango(
    actual: Rango,
    onRangoSeleccionado: (Rango)->Unit,
    modifier: Modifier = Modifier
){
    Row(modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp))
    {
        Rango.values().forEach { rango ->
            FilterChip(
                selected = rango == actual,
                onClick = {onRangoSeleccionado(rango)},
                label = {Text(rango.label)},
            )
        }

    }


}
private fun agruparPorCategoria(
    gastos: List<ExpensesModel>,
    rango: Rango
): Map<String, Double> {
    val ahora = System.currentTimeMillis()
    val desde = ahora - rango.millis
    return gastos.filter { it.fecha.time in desde..ahora }
        .groupBy { it.categoria }
        .mapValues { (_, list) -> list.sumOf { it.importe } }
        .toSortedMap()
}

enum class Rango(val label: String, val millis: Long) {
    SEMANA("Semana", 7L * 24 * 60 * 60 * 1000),
    MES("Mes", 30L * 24 * 60 * 60 * 1000),
    ANIO("Año", 365L * 24 * 60 * 60 * 1000)
}*/