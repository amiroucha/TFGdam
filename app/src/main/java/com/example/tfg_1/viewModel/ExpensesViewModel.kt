package com.example.tfg_1.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_1.R
import com.example.tfg_1.model.DataChart
import com.example.tfg_1.model.ExpensesModel
import com.example.tfg_1.model.PeriodoFiltro
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import com.example.tfg_1.repositories.UserRepository
import java.text.SimpleDateFormat
import java.util.*

class ExpensesViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    private var gastos by mutableStateOf<List<ExpensesModel>>(emptyList())

    //lista de gastos filtrados por usuario
    var gastosFiltrados by mutableStateOf<List<ExpensesModel>>(emptyList())
        private set

    var loading by mutableStateOf(true)
        private set

    private var listenerRegistration: ListenerRegistration? = null
    private var homeIdBD: String? = null

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    // Filtros de usuario
    var usuarioFiltrado by mutableStateOf<String?>(null)

    var usuarios by mutableStateOf<List<String>>(emptyList())

    init {
        escucharUsuario()
    }
    //para logs
    companion object {
        private const val TAG = "ExpensesViewModel"
    }

    private fun actualizarGastosFiltrados() {
        gastosFiltrados = gastos.filter { gasto ->
            usuarioFiltrado == null || gasto.asignadoA == usuarioFiltrado
        }
    }

    fun modificaUsuarioFiltrado(usuario: String?) {
        usuarioFiltrado = usuario
        actualizarGastosFiltrados()
    }

    //escuchar cambios en el usuario
    //si el homeID de ese usuario no es = al que habia guardado se act
    private fun escucharUsuario() {
        val uid = auth.currentUser?.uid

        if (uid == null) {//en caso de que el usuario no este logueado
            loading = false
            Log.w(TAG, "Usuario no autenticado")
            viewModelScope.launch { _uiEvent.emit(UiEvent.Error("Usuario no autenticado")) }
            return
        }
        userRepository.escucharHomeIdUsuarioActual { homeId ->
            //si es null me voy
            if (homeId == null) {
                loading = false
                viewModelScope.launch {
                    _uiEvent.emit(UiEvent.Error("Error leyendo usuario o homeId"))
                }
                return@escucharHomeIdUsuarioActual
            }

            //si no es igual actualizo
            if (homeIdBD != homeId) {
                homeIdBD = homeId
                Log.d(TAG, "homeId actualizado, llamo a escucharGastos()")
                //recojo los gatsos de ese hogar
                listenerRegistration?.remove()
                listenerRegistration = userRepository.escucharGastos(homeId) { nuevosGastos ->
                    gastos = nuevosGastos
                    actualizarGastosFiltrados()
                    loading = false
                }

                //cargo los usuarios que tiene
                viewModelScope.launch {
                    try{
                        val miembros = userRepository.getMembersByHomeId(homeId)
                        usuarios = miembros.map { it.name }
                    }catch (e: Exception) {
                        Log.e(TAG, "Error al cargar usuarios", e)
                        _uiEvent.emit(UiEvent.Error("Error al cargar usuarios: ${e.localizedMessage}"))
                    }
                }
            }
        }
    }

    //limpiar el recordatorio
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
    //añadir un gasto
    suspend fun aniadirGastoVM(
        categoria: String,
        descripcion: String,
        importe: Double,
        fecha: Date,
        asignadoA: String): Boolean
    {
        val homeId = homeIdBD ?: return false
        val gasto = ExpensesModel(
            categoria = categoria.lowercase(),
            descripcion = descripcion,
            importe = importe,
            fecha = fecha,
            asignadoA = asignadoA
        )
        val success = userRepository.addExpense(homeId, gasto)
        if (success){
            _uiEvent.emit(UiEvent.Added)
            return true
        }
        else {
            _uiEvent.emit(UiEvent.Error("Error al guardar gasto"))
            return false
        }
    }

    //eliminar un gasto
    fun eliminarGasto(gasto: ExpensesModel, context: Context) {
        viewModelScope.launch {
            val usuarioActual = userRepository.getCurrentUserName().trim().lowercase()
            val asignado = gasto.asignadoA.trim().lowercase()

            // Si el gasto está asignado a != del usuario actual y no es "hogar"
            // no permitir eliminar
            if (asignado != "hogar" && asignado != usuarioActual) {
                viewModelScope.launch {
                    _uiEvent.emit(UiEvent.Error(context.getString(R.string.nopermiso_eliminar)))
                }
                return@launch
            }

            val success = userRepository.deleteExpense(homeIdBD ?: return@launch, gasto.id)
            if (!success) _uiEvent.emit(UiEvent.Error(context.getString(R.string.error_al_eliminar_gasto)))
        }
    }


    fun agruparGastosPorPeriodo(gastos: List<ExpensesModel>, filtro: PeriodoFiltro): List<DataChart> {
        val calendar = Calendar.getInstance()

        val agrupados: Map<Long, List<ExpensesModel>> = when (filtro) {
            PeriodoFiltro.SEMANA -> { // Agrupación por semna
                //fecha del primer día de la semana de ese gasto
                gastos.groupBy { gasto ->
                    calendar.time = gasto.fecha // ajusto calendario al primer día de la semana
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    //reseteo horas, minutos, segundos y milisegundos a cero para que no se ralle
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    //timespam como clave para agrupar
                    calendar.timeInMillis
                }
            }
            PeriodoFiltro.MES -> { // Agrupación por mes
                gastos.groupBy { gasto ->
                    calendar.time = gasto.fecha // fecha cuando se gasta
                    calendar.set(Calendar.DAY_OF_MONTH, 1) // dia uno del mes
                    //reseteo valores
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
                    //reseteo valores
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0) // Reset de tiempo
                    calendar.timeInMillis // Timestamp como clave
                }
            }
        }

        // transformar mapa agrupado en lista ordenada de DataChart
        return agrupados.entries.sortedBy { it.key }.map { (timeMillis, listaGastos) ->
            // total de los gastos (campo importes)
            val total = listaGastos.sumOf { it.importe }

            // Se establece la fecha del grupo
            calendar.timeInMillis = timeMillis

            val label = when (filtro) { //etiqueta segn filtro semana, mes,año
                PeriodoFiltro.SEMANA -> {
                    val semana = calendar.get(Calendar.WEEK_OF_YEAR) // numero semana
                    val anio = calendar.get(Calendar.YEAR) % 100 // ultimos 2 dígitos del año
                    "$semana-$anio"
                }
                PeriodoFiltro.MES -> {
                    val anio = calendar.get(Calendar.YEAR)
                    val mesNombre = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time) // nombre mes
                    "$mesNombre-$anio"
                }
                PeriodoFiltro.ANIO -> {
                    val anio = calendar.get(Calendar.YEAR)
                    "$anio"
                }
            }
            //mando un objeto DataChart con la etiqueta y el total para el gráfico visual
            DataChart(label = label, value = total.toFloat())
        }
    }

    //gastos totales desglosados
    fun obtenerGastosPorLabel(
        label: String,
        filtro: PeriodoFiltro,
        gastos: List<ExpensesModel>
    ): List<ExpensesModel> {
        val calendar = Calendar.getInstance()

        // Filtro la lista de gastos para obtener solo los
        // que coinciden con la etiqueta del periodo
        return gastos.filter { gasto ->
            calendar.time = gasto.fecha
            // Genero la etiqueta del gasto según el filtro
            // comparo con la etiqueta recibida
            val gastoLabel = when (filtro) {
                PeriodoFiltro.SEMANA -> {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val semana = calendar.get(Calendar.WEEK_OF_YEAR)
                    val anio = calendar.get(Calendar.YEAR) % 100
                    "$semana-$anio"
                }
                PeriodoFiltro.MES -> {
                    val anio = calendar.get(Calendar.YEAR)
                    val mesNombre = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
                    "$mesNombre-$anio"
                }
                PeriodoFiltro.ANIO -> {
                    calendar.get(Calendar.YEAR).toString()
                }
            }

            // Compara  etiqueta generada para el gasto = etiqueta del periodo seleccionado
            // para filtrar solo los gastos = periodo agrupado semana/ mes / año.
            gastoLabel == label
        }
    }


    sealed interface UiEvent {
        data object Added : UiEvent
        data class Error(val msg: String) : UiEvent
    }
}
