package com.example.tfg_1.ui.ui

import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg_1.R
import com.example.tfg_1.model.DataChart
import com.example.tfg_1.model.ExpensesModel
import com.example.tfg_1.viewModel.ExpensesViewModel
import com.github.mikephil.charting.data.Entry
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen() {
    val vm: ExpensesViewModel = viewModel()

    val gastos by remember { derivedStateOf { vm.gastos } } //estado lista gastos
    val loading by remember { derivedStateOf { vm.loading } } //estado carga
    val gastosFiltrados by remember { derivedStateOf { vm.gastosFiltrados } } //lista de gastos filtrada

    var periodoFiltro by remember { mutableStateOf(PeriodoFiltro.MES) } // por defecto es mensual

    // agrupa gastos por semana, mes o año
    fun agruparGastosPorPeriodo(gastos: List<ExpensesModel>, filtro: PeriodoFiltro): List<DataChart> {
        val calendar = Calendar.getInstance()

        val agrupados: Map<Long, List<ExpensesModel>> = when (filtro) {
            PeriodoFiltro.SEMANA -> { // Agrupación por semna
                gastos.groupBy { gasto ->
                    calendar.time = gasto.fecha // fecha del gasto al calendario
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek) // ajusto primer dia de semana
                    calendar.timeInMillis
                }
            }
            PeriodoFiltro.MES -> { // Agrupación por mes
                gastos.groupBy { gasto ->
                    calendar.time = gasto.fecha // fecha cuando se gasta
                    calendar.set(Calendar.DAY_OF_MONTH, 1) // dia uno del mes
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0) // reseteo la hora
                    calendar.timeInMillis // Timestamp como clave
                }
            }
            PeriodoFiltro.ANIO -> { // Agrupación por año
                gastos.groupBy { gasto ->
                    calendar.time = gasto.fecha // fecha cuando se gasta
                    calendar.set(Calendar.DAY_OF_YEAR, 1) // Día uno del año
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0) // Reset de tiempo
                    calendar.timeInMillis // Timestamp como clave
                }
            }
        }

        // Se transforma el mapa agrupado en una lista ordenada de DataChart
        return agrupados.entries.sortedBy { it.key }.map { (timeMillis, listaGastos) ->

            val total = listaGastos.sumOf { it.importe } // total de los gastos (campo iportes)

            calendar.timeInMillis = timeMillis // Se establece la fecha del grupo

            val label = when (filtro) { //etiqueta segn filtro semana, mes,año
                PeriodoFiltro.SEMANA -> {

                    val semana = calendar.get(Calendar.WEEK_OF_YEAR) // numero semana
                    val anio = calendar.get(Calendar.YEAR) % 100 // ultimos 2 dígitos del año
                    "$semana-$anio"

                }
                PeriodoFiltro.MES -> {

                    val anio = calendar.get(Calendar.YEAR)
                    val mesNombre = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time) // nombre mes
                    "$mesNombre $anio" // Ejemplo: "May 2025"
                }
                PeriodoFiltro.ANIO -> calendar.get(Calendar.YEAR).toString()
            }
            DataChart(label = label, value = total.toFloat())
        }
    }

    val datosChart = remember(gastos, periodoFiltro) {
        agruparGastosPorPeriodo(gastosFiltrados, periodoFiltro) // agrupo y guardo/recuerdo datos para el gráfico
    }

    var expandedFiltro by remember { mutableStateOf(false) } //menú desplegable está abierto??
    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    //escuchar eventos dee ViewModel
    LaunchedEffect(Unit) {
        vm.uiEvent.collect { ev ->
            when (ev) {
                ExpensesViewModel.UiEvent.Added ->
                    snackbarHostState.showSnackbar("Gasto guardado") //salio bien
                is ExpensesViewModel.UiEvent.Error ->
                    snackbarHostState.showSnackbar(ev.msg) //error con descripcion
            }
        }
    }

    val categorias = listOf("comida", "agua", "luz", "limpieza", "aseo", "gas", "otra") //lista de categorías
    var categoria by remember { mutableStateOf(categorias.first()) } // categoría seleccionada
    var otraCategoria by remember { mutableStateOf("") } //categoría personalizada
    var descripcion by remember { mutableStateOf("") } // Descripción
    var importeTxt by remember { mutableStateOf("") } // Importe
    val fecha by remember { mutableStateOf(Calendar.getInstance().time) } // Fecha

    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) } // Formateador de fecha

    //guarda gasto
    suspend fun save() {
        val imp = importeTxt.toDoubleOrNull() ?: 0.0 // Se convierte el texto a número
        val cat = if (categoria == "otra") otraCategoria else categoria // Se determina la categoría
        if (cat.isBlank() || imp <= 0) { // Validación básica
            snackbarHostState.showSnackbar("Completa categoría e importe")
            return
        }
        if (vm.aniadirGastoVM(cat, descripcion, imp, fecha)) { // Se guarda el gasto en el ViewModel
            categoria = categorias.first() // Se reinician los campos
            otraCategoria = ""
            descripcion = ""
            importeTxt = ""
        }
    }

    //  ui-------------------------------------------------------------
    Box(modifier = Modifier.fillMaxSize()) {
        // Lista de gastos
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            // Encabezado = gráfico y filtro fecha
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(expanded = expandedFiltro, onExpandedChange = { expandedFiltro = !expandedFiltro }) {
                        TextField(
                            readOnly = true,
                            value = periodoFiltro.name,//asigno nombre de "mes/semna/año"
                            onValueChange = {},
                            label = { Text("Filtro de tiempo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedFiltro) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedFiltro, onDismissRequest = { expandedFiltro = false }) {
                            PeriodoFiltro.entries.forEach { filtro ->
                                DropdownMenuItem(
                                    text = { Text(filtro.name) },
                                    onClick = {
                                        periodoFiltro = filtro // Cambia el filtro
                                        expandedFiltro = false // Cierra el menú
                                    }
                                )
                            }
                        }
                    }
                }

                Chart(datosChart) // muestra el gráfico con dato

                Spacer(modifier = Modifier.height(10.dp)) // Espacio

                Text(
                    text = stringResource(R.string.lista_de_gastos),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // lista de gastos filtrados
            items(gastosFiltrados.sortedByDescending { it.fecha }) { g ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //creacion de nuevos gastos
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${g.categoria.replaceFirstChar { it.uppercase() }}: ${"%.2f".format(g.importe)}€  ·  ${dateFmt.format(g.fecha)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (g.descripcion.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(text = g.descripcion, style = MaterialTheme.typography.bodyLarge)
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

        // Botón añadir gasto
        Button(
            onClick = { showDialog = true },
            enabled = !loading,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Text(stringResource(R.string.aniadir_gastoBT))
        }


        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nuevo Gasto") },
                text = {
                    Column {
                        TextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = importeTxt,
                            onValueChange = { importeTxt = it },
                            label = { Text("Importe") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Puedes agregar campos extra como categoría, fecha, etc.
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        CoroutineScope(Dispatchers.Main).launch {
                            save()
                        }
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Snackbar para mensajes flotantes
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )

        // Cargando indicador circular
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
fun Chart(datos: List<DataChart>) { //DAtaChart = label and value
    val context = LocalContext.current

    AndroidView( //para introducir vista "tradi" de Android
        modifier = Modifier
            .fillMaxWidth()//ancho y alto del gráfico
            .height(300.dp),
        factory = {
            com.github.mikephil.charting.charts.LineChart(context).apply {//libreria - MPAndroidChart
                //limtaciones tamaño grafico = ocupa tod espacio del contenedor
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(android.graphics.Color.WHITE) //fondo blanco del grfa
                setNoDataText("No hay datos") //si no hay gastos, aviso

                //Eje y izq
                axisLeft.apply {
                    axisMinimum = 0f
                    setDrawGridLines(true)
                }
                //Eje y derecha no lo quiero
                axisRight.isEnabled = false

                //eje x va bottom
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false) //sin lineas para arriba
                }
                //no quiero q se vean
                description.isEnabled = false
                legend.isEnabled = false
            }
        },
        //cada cambio lo act
        update = { chart ->
            val entries = mutableListOf<Entry>() //puntos del grafico
            val labels = mutableListOf<String>() //eje x label
            datos.forEachIndexed { index, punto -> //index=posicion, punto = valor
                entries.add(Entry(index.toFloat(), punto.value))
                labels.add(punto.label)
                //en la posicion x muestro el valor i
            }

            val dataSet = LineDataSet(entries, "Gastos").apply {
                color = android.graphics.Color.BLUE
                valueTextColor = android.graphics.Color.BLACK
                setDrawCircles(true)
                setDrawValues(true)
                lineWidth = 2f
                circleRadius = 4f
            }
            //formato etiquetas eje x
            val formatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index in labels.indices) labels[index] else ""
                    //index valido = label, index invalido = ""
                }
            }

            chart.xAxis.apply {
                valueFormatter = formatter
                labelRotationAngle = -45f //roto 45º
                granularity = 1f //solo tipo int
                setLabelCount(labels.size, true) //intenta enseñar todas labels
            }

            chart.data = LineData(dataSet)
            chart.invalidate()//act dibujo con nuevos datos


        }
    )
}
