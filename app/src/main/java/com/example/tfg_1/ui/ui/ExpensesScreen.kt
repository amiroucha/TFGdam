package com.example.tfg_1.ui.ui


import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import com.example.tfg_1.model.PeriodoFiltro
import com.example.tfg_1.model.toDisplayString
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen() {
    val vm: ExpensesViewModel = viewModel()
    val context = LocalContext.current

    val loading by remember { derivedStateOf { vm.loading } } //estado carga
    //lista de gastos filtrada
    val gastosFiltrados by remember { derivedStateOf { vm.gastosFiltrados } }

    //btn elimnar gasto
    var gastoAEliminar by remember { mutableStateOf<ExpensesModel?>(null) }
    var showConfirmDelete by remember { mutableStateOf(false) }


    // por defecto es mensual
    var periodoFiltro by remember { mutableStateOf(PeriodoFiltro.MES) }


    // agrupa gastos por semana, mes o año


    var expandedFiltroFecha by remember { mutableStateOf(false) } //menú desplegable está abierto??
    var expandedCategoria by remember { mutableStateOf(false) }     // para categoría en el diálogo

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

    val categorias = listOf(
        stringResource(R.string.comida),
        stringResource(R.string.agua),
        stringResource(R.string.luz),
        stringResource(R.string.limpieza),
        stringResource(R.string.aseo),
        stringResource(R.string.gas),
        stringResource(R.string.otra)
    ) //lista de categorías
    var categoria by remember { mutableStateOf(categorias.first()) } // categoría seleccionada
    var otraCategoria by remember { mutableStateOf("") } //categoría personalizada
    var descripcion by remember { mutableStateOf("") } // Descripción
    var importe by remember { mutableStateOf("") } // Importe
    val usuariosAsignar = listOf("Hogar") + vm.usuarios ///añado opcion "Hogar"
    var asignadaA by remember { mutableStateOf(usuariosAsignar.first()) } // asigando a ...
    var fecha by remember { mutableStateOf(Calendar.getInstance().time) } // Fecha
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) } // Formateador de fecha

    //guarda gasto
    suspend fun save() {
        val imp = importe.toDoubleOrNull() ?: 0.0 // Se convierte el texto a número
        val cat = if (categoria == "otra") otraCategoria else categoria // Se determina la categoría
        if (cat.isBlank() || imp <= 0) { // Validación básica
            snackbarHostState.showSnackbar("Completa categoría e importe")
            return
        }
        if (vm.aniadirGastoVM(cat, descripcion, imp, fecha, asignadaA )) { // Se guarda el gasto en el ViewModel
            categoria = categorias.first() // Se reinician los campos
            otraCategoria = ""
            descripcion = ""
            importe = ""
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
                //titulo encima de grafico:
                Text(
                    text = stringResource(R.string.gastos_totales_del_hogar),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(top = 9.dp, bottom = 7.dp, start = 15.dp),
                    fontSize = 19.sp
                )
                Spacer(modifier = Modifier.height(10.dp)) // Espacio


                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedFiltroFecha,
                        onExpandedChange = { expandedFiltroFecha = !expandedFiltroFecha }
                    ) {
                        TextField(
                            readOnly = true,
                            value = periodoFiltro.toDisplayString(context),
                            onValueChange = {},
                            label = { Text(stringResource(R.string.filtro_de_tiempo)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedFiltroFecha) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFiltroFecha,
                            onDismissRequest = { expandedFiltroFecha = false }
                        ) {
                            PeriodoFiltro.entries.forEach { filtro ->
                                DropdownMenuItem(
                                    text = { Text(filtro.toDisplayString(context)) },
                                    onClick = {
                                        periodoFiltro = filtro
                                        expandedFiltroFecha = false
                                    }
                                )
                            }
                        }
                    }
                }

                val datosChart = remember(gastosFiltrados, periodoFiltro) {
                    vm.agruparGastosPorPeriodo(gastosFiltrados, periodoFiltro)
                }

                Chart(datosChart) // muestra el gráfico con dato

                Spacer(modifier = Modifier.height(10.dp)) // Espacio
                //lista de los gastos:
                Text(
                    text = stringResource(R.string.lista_de_gastos),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(top = 9.dp, bottom = 7.dp, start = 15.dp),
                    fontSize = 19.sp
                )
            }

            // lista de gastos filtrados, tarjeta
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
                        //creacion de nuevos gastos
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${g.categoria.replaceFirstChar { it.uppercase() }}: ${"%.2f".format(g.importe)}€ -- ${dateFmt.format(g.fecha)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 16.sp
                            )
                            if (g.descripcion.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(text = g.descripcion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 15.sp
                                )
                            }

                            Text(text = "Asignado a: ${g.asignadoA}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 15.sp)

                        }
                        IconButton(onClick = {
                            gastoAEliminar = g
                            showConfirmDelete = true}
                        ) {
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
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.aniadir_gastoBT))
        }


        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(id = R.string.nuevogastos)) },
                text = {
                    Column {
                        // Categoría
                        ExposedDropdownMenuBox(
                            expanded = expandedCategoria,
                            onExpandedChange = { expandedCategoria = !expandedCategoria }
                        ) {
                            TextField(
                                value = categoria,
                                onValueChange = {},
                                label = { Text(stringResource(id = R.string.categoria)) },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategoria) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedCategoria,
                                onDismissRequest = { expandedCategoria = false }
                            ) {
                                categorias.forEach {
                                    DropdownMenuItem(
                                        text = { Text(it) },
                                        onClick = {
                                            categoria = it
                                            expandedCategoria = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // campo adicional si es "otra"
                        if (categoria == "otra") {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = otraCategoria,
                                onValueChange = { otraCategoria = it },
                                label = { Text(stringResource(id = R.string.otra_categoria)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        //descripcion
                        TextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text(stringResource(id = R.string.descripcion)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        //importe
                        TextField(
                            value = importe,
                            onValueChange = { importe = it },
                            label = { Text(stringResource(id = R.string.importe)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // fecha
                        val calendar = Calendar.getInstance().apply { time = fecha }
                        if (showDatePicker) {
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCalendar = Calendar.getInstance()
                                    selectedCalendar.set(year, month, dayOfMonth)
                                    fecha = selectedCalendar.time
                                    showDatePicker = false
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                            showDatePicker=false // evitar que se reabra solo
                        }
                        //manejar el clic
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                        ) {
                            //=estilo
                            TextField(
                                value = dateFmt.format(fecha),
                                onValueChange = {},
                                label = { Text(stringResource(id = R.string.seleccionar_fecha)) },
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker=true }) {
                                        Icon(
                                            imageVector = Icons.Filled.CalendarToday,
                                            contentDescription = stringResource(R.string.seleccionaFecha)
                                        )
                                    }
                                },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = true,
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Asignar gasto a----------------------------------
                        var expandedAsignadoA by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expandedAsignadoA,
                            onExpandedChange = { expandedAsignadoA = !expandedAsignadoA }
                        ) {
                            TextField(
                                value = asignadaA,
                                onValueChange = {},
                                label = { Text(stringResource(R.string.asignar_gasto_a)) },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedAsignadoA) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedAsignadoA,
                                onDismissRequest = { expandedAsignadoA = false }
                            ) {
                                usuariosAsignar.forEach { usuario ->
                                    DropdownMenuItem(
                                        text = { Text(usuario) },
                                        onClick = {
                                            asignadaA = usuario
                                            expandedAsignadoA = false
                                        }
                                    )
                                }
                            }

                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            if (categoria.isNotBlank() && importe.isNotBlank() && asignadaA.isNotBlank()) {
                                save()
                                showDialog = false
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.completaTodosCampos),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }) {
                        Text(stringResource(id = R.string.guardar))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(id = R.string.cancelar))
                    }
                }
            )
        }

        //alerta para asegurar eliminar ese gasto
        if (showConfirmDelete && gastoAEliminar != null) {
            AlertDialog(
                onDismissRequest = {
                    showConfirmDelete = false
                    gastoAEliminar = null
                },
                title = { Text(stringResource(id = R.string.eliminar_gasto)) },
                text = { Text(stringResource(R.string.seguroEliminarGasto)) },
                confirmButton = {
                    TextButton(onClick = {
                        vm.eliminarGasto(gastoAEliminar!!) // elimina gasto
                        showConfirmDelete = false
                        gastoAEliminar = null
                    }) {
                        Text(stringResource(id = R.string.eliminar_gasto), color = colorResource(id = R.color.red))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showConfirmDelete = false
                        gastoAEliminar = null
                    }) {
                        Text(stringResource(id = R.string.cancelar))
                    }
                }
            )
        }

        // Snackbar para mensajes flotantes
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
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
                setNoDataText(context.getString(R.string.no_hay_datos)) //si no hay gastos, aviso

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
                setScaleEnabled(false) //zoom haciedno 2 dedos
                isDoubleTapToZoomEnabled = true  //zoom dos toques
                isDragEnabled = true //scroll horizonal
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
            //dar informacion al tocar los puntos
            chart.setOnChartValueSelectedListener(
                object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.let {
                            val index = e.x.toInt()
                            if (index in labels.indices) {
                                val labelSeleccionada = labels[index]
                                val valor = e.y
                                Toast.makeText(
                                    context,
                                    " -  $labelSeleccionada // ${"%.2f".format(valor)}€",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onNothingSelected() {
                        // nada
                    }
            })
            chart.invalidate()//act dibujo con nuevos datos

        }
    )
}
