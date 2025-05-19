package com.example.tfg_1.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg_1.R
import com.example.tfg_1.model.ExpensesModel
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

    /* ---------- Snackbar ---------- */
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

    /* ---------- estados del formulario ---------- */
    val categorias = listOf("comida", "agua", "luz", "limpieza", "aseo", "gas", "otra")
    var categoria    by remember { mutableStateOf(categorias.first()) }
    var otraCategoria by remember { mutableStateOf("") }
    var descripcion  by remember { mutableStateOf("") }
    var importeTxt   by remember { mutableStateOf("") }
    var fecha        by remember { mutableStateOf(Calendar.getInstance().time) }
    var showPicker   by remember { mutableStateOf(false) }
    val dateFmt      = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val scope        = rememberCoroutineScope()

    /* ---------- guardar ---------- */
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
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->

        if (loading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {
            /* -------- Título -------- */
            Text(
                text = stringResource(R.string.nuevogastos),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            /* -------- Categoría -------- */
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                TextField(
                    value = categoria,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.categoria)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categorias.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {            // ← añade onClick
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
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            /* -------- Descripción -------- */
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text(stringResource(R.string.descripcion)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            /* -------- Importe -------- */
            OutlinedTextField(
                value = importeTxt,
                onValueChange = { importeTxt = it },
                label = { Text("Importe (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            /* -------- Fecha -------- */
            Text(
                text = stringResource(R.string.selecciona_una_fecha),
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(onClick = { showPicker = true }) {
                Text(dateFmt.format(fecha))
            }
            val datePickerState = rememberDatePickerState(fecha.time)
            if (showPicker) {
                DatePickerDialog(
                    onDismissRequest = { showPicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                fecha = Date(millis)
                            }
                            showPicker = false
                        }) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
                    }
                ) { DatePicker(state = datePickerState) }
            }

            Spacer(Modifier.height(16.dp))

            /* -------- Lista de gastos -------- */
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 72.dp)
            ) {
                items(gastos.sortedByDescending { it.fecha }) { g ->
                    Text(
                        "${g.categoria.replaceFirstChar { it.uppercase() }}: " +
                                "${"%.2f".format(g.importe)}€  ·  ${dateFmt.format(g.fecha)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (g.descripcion.isNotBlank())
                        Text(g.descripcion, style = MaterialTheme.typography.bodySmall)
                    Divider()
                }
            }

            /* -------- Botón guardar -------- */
            Button(
                onClick = { scope.launch { save() } },
                enabled = !loading,           // deshabilitado mientras carga
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.aniadir_gastoBT))
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