package com.example.tfg_1.ui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.tfg_1.model.DataChart
import com.example.tfg_1.model.ExpensesModel
import com.example.tfg_1.viewModel.ExpensesViewModel
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen() {
    val vm: ExpensesViewModel = viewModel()
    val gastos by remember { derivedStateOf { vm.gastos } }
    val loading by remember { derivedStateOf { vm.loading } }
    val gastosFiltrados by remember { derivedStateOf { vm.gastosFiltrados } }

    var periodoFiltro by remember { mutableStateOf(PeriodoFiltro.MES) }

    fun agruparGastosPorPeriodo(gastos: List<ExpensesModel>, filtro: PeriodoFiltro): List<DataChart> {
        val calendar = Calendar.getInstance()
        val agrupados:Map<Long, List<ExpensesModel>> = when (filtro) {
            PeriodoFiltro.SEMANA -> {
                // Clave: primer día de la semana
                gastos.groupBy { gasto ->
                    calendar.time = gasto.fecha
                    val anio = calendar.get(Calendar.YEAR)
                    // lunes sea primer día de semana
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.timeInMillis
                }
            }
            PeriodoFiltro.MES -> {
                // Clave: primer día del mes
                gastos.groupBy { gasto ->
                    calendar.time = gasto.fecha
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
            }
            PeriodoFiltro.ANIO -> {
                // Clave: primer día del año
                gastos.groupBy { gasto ->
                    calendar.time = gasto.fecha
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
            }
        }

        return agrupados.entries
            .sortedBy { it.key }  // orden cronológico ascendente
            .map { (timeMillis, listaGastos) ->
                val total = listaGastos.sumOf { it.importe }
                calendar.timeInMillis = timeMillis
            val label = when (filtro) {
                PeriodoFiltro.SEMANA -> {
                    val semana = calendar.get(Calendar.WEEK_OF_YEAR)
                    val anio = calendar.get(Calendar.YEAR) % 100  // me quedo con dos ultimos dígitos
                    "$semana-$anio"
                }
                PeriodoFiltro.MES -> {
                    val anio = calendar.get(Calendar.YEAR)
                    val mesNombre = SimpleDateFormat("MMM", Locale.getDefault())
                        .format(calendar.time)
                    "$mesNombre $anio"
                }
                PeriodoFiltro.ANIO -> {
                    calendar.get(Calendar.YEAR).toString()
                }
            }
            DataChart(label = label, value = total.toFloat())
        }
    }

    val datosChart = remember(gastos, periodoFiltro) {
        agruparGastosPorPeriodo(gastosFiltrados, periodoFiltro)
    }

    var expandedFiltro by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
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

    val categorias = listOf("comida", "agua", "luz", "limpieza", "aseo", "gas", "otra")
    var categoria by remember { mutableStateOf(categorias.first()) }
    var otraCategoria by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var importeTxt by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(Calendar.getInstance().time) }
    var showPicker by remember { mutableStateOf(false) }
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val datePickerState = rememberDatePickerState(fecha.time)
    val scope = rememberCoroutineScope()

    suspend fun save() {
        val imp = importeTxt.toDoubleOrNull() ?: 0.0
        val cat = if (categoria == "otra") otraCategoria else categoria
        if (cat.isBlank() || imp <= 0) {
            snackbarHostState.showSnackbar("Completa categoría e importe")
            return
        }
        if (vm.aniadirGastoVM(cat, descripcion, imp, fecha)) {
            categoria = categorias.first()
            otraCategoria = ""
            descripcion = ""
            importeTxt = ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(expanded = expandedFiltro, onExpandedChange = { expandedFiltro = !expandedFiltro }) {
                        TextField(
                            readOnly = true,
                            value = periodoFiltro.name,
                            onValueChange = {},
                            label = { Text("Filtro de tiempo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedFiltro) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedFiltro, onDismissRequest = { expandedFiltro = false }) {
                            PeriodoFiltro.values().forEach { filtro ->
                                DropdownMenuItem(
                                    text = { Text(filtro.name) },
                                    onClick = {
                                        periodoFiltro = filtro
                                        expandedFiltro = false
                                    }
                                )
                            }
                        }
                    }
                }

                Chart(datosChart)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.lista_de_gastos),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(gastosFiltrados.sortedByDescending { it.fecha }) { g ->
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
                    ) {
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

        Button(
            onClick = { showDialog = true },
            enabled = !loading,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.aniadir_gastoBT))
        }

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

                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text(stringResource(R.string.descripcion)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )

                        OutlinedTextField(
                            value = importeTxt,
                            onValueChange = { importeTxt = it },
                            label = { Text("Importe (€)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )

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
                                    }) {
                                        Text(stringResource(R.string.aceptar))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showPicker = false }) {
                                        Text(stringResource(R.string.cancelar))
                                    }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

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

enum class PeriodoFiltro { SEMANA, MES, ANIO }

@Composable
fun Chart(datos: List<DataChart>) {
    val puntos = datos.map { dataChart ->
        //evitar numeros negativos eje y
        val safeValue = dataChart.value.coerceAtLeast(0f) // no menos que 0
        LineChartData.Point(
            value = safeValue,
            label = dataChart.label
        )
    }

    val lineas = listOf(
        LineChartData(
            points = puntos,
            lineDrawer = SolidLineDrawer()
        )
    )

    LineChart(
        linesChartData = lineas,
        modifier = Modifier
            .padding(horizontal = 30.dp, vertical = 80.dp)
            .height(300.dp)
            .fillMaxWidth()
    )
}
