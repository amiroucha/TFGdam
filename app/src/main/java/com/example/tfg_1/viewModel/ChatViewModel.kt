package com.example.tfg_1.viewModel

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.graphics.drawable.IconCompat
import androidx.core.app.Person
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_1.MyApp
import com.example.tfg_1.R
import com.example.tfg_1.model.ChatMessageModel
import com.example.tfg_1.notifications.PreferenceMnger
import com.example.tfg_1.repositories.UserRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private var _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _messages = mutableStateListOf<ChatMessageModel>()
    private val messages: List<ChatMessageModel> = _messages

    private var listenerRegistration: ListenerRegistration? = null

    var currentUserId: String = ""
    private var currentUserName: String = ""

    private val _searchQuery = MutableStateFlow("")
    private val searchQuery: StateFlow<String> = _searchQuery

    val filteredMessages: List<ChatMessageModel>
        get() = if (searchQuery.value.isBlank()) {
            messages
        } else {
            messages.filter {
                it.text.contains(searchQuery.value, ignoreCase = true) ||
                        it.senderName.contains(searchQuery.value, ignoreCase = true)
            }
        }

    //ultimo mensaje enviado, para contrlar notificaciones
    private var lastNotifiedTimestamp: Long = 0L
    private lateinit var preferencesManager: PreferenceMnger


    fun init(context: Context) {
        preferencesManager = PreferenceMnger(context)
        lastNotifiedTimestamp = preferencesManager.getLastNotifiedTimestamp()
        Log.d("ChatViewModel", "Timestamp leído desde SharedPreferences: $lastNotifiedTimestamp")

    }

    //colores para los bocadillos
    private val userColors = mutableMapOf<String, Color>()
    private val availableColors = listOf(
            Color(0xFFEFCFBF), // naranja
            Color(0xFFB2D6F2), // azul
            Color(0xFFADF8C3), // verde
            Color(0xFFF2CCE0), // rosa
            Color(0xFFF4E1BE), // amarillo
            Color(0xFFD3B9F2), // lila
            Color(0xFFFFB0B0), // lila
            Color(0xFFA0E0CA), // lila
    )


    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    //cargo los mensajes
    fun loadChat(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            currentUserId = userRepository.getCurrentUserId()
            currentUserName = userRepository.getCurrentUserName()
            val homeId = userRepository.getCurrentUserHomeId()

            listenerRegistration?.remove()//evitar duplicaciones

            listenerRegistration = userRepository.escucharMensajes(homeId) { mensajes ->
                Log.d("ChatViewModel", "Mensajes recibidos: ${mensajes.size}")

                //mensajes != del user actual y mas recientes que el ultimo mensaje notificado
                val mensajesNuevos = mensajes.filter {
                    it.senderId != currentUserId && it.timestamp > lastNotifiedTimestamp
                }

                mensajesNuevos.forEach { mensaje -> //se envia de manera local las notificaciones
                    //por cada mensaje
                    sendLocalNotification(
                        context = context,
                        senderName = mensaje.senderName,
                        message = mensaje.text
                    )
                }

                if (mensajes.isNotEmpty()) {
                    //mayor tiempo entre los mensajes/ ultimo mensaje
                    val maxTimestamp = mensajes.maxOf { it.timestamp }
                    if (maxTimestamp > lastNotifiedTimestamp) { //tiempo > al ultimoREgistrado
                        lastNotifiedTimestamp = maxTimestamp //actualizo tiempo del ultimo mensaje notificado
                        //Se guarda el nuevo timestamp en SharedPreferences
                        preferencesManager.setLastNotifiedTimestamp(lastNotifiedTimestamp)
                        Log.d("ChatViewModel", "Actualizado timestamp: $lastNotifiedTimestamp")
                    }
                }

                _messages.clear()
                _messages.addAll(mensajes)
                _isLoading.value = false
            }
        }
    }

    //gaurdo los mensajes
    fun sendMessage(text: String) {
        viewModelScope.launch {
            val homeId = userRepository.getCurrentUserHomeId()

            val message = ChatMessageModel(
                senderId = currentUserId,
                senderName = currentUserName,
                text = text,
                timestamp = System.currentTimeMillis(),
            )

            userRepository.enviarMensaje(homeId, message)

        }
    }

    // Notificación local
    private fun sendLocalNotification(context: Context, senderName: String, message: String) {
        //servicio del sistema que gestiona las notificaciones
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //mi canal creado a usar
        val channelId = MyApp.NOTIFICATION_ID

        val person = Person.Builder() //Person , nombre e icono
            .setName(senderName)
            .setIcon(IconCompat.createWithResource(context, R.drawable.logotfg)) // tu logo
            .build()

        // Estilo tipo chat con encabezado FlowHome
        val messageStyle = NotificationCompat.MessagingStyle(person)
            .setConversationTitle("FLOWHOME") // Encabezado
            .addMessage(message, System.currentTimeMillis(), person) // Mensaje con hora y autor

        //construccion de la notificacion
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logotfg) // icono superior
            .setStyle(messageStyle)
            .setAutoCancel(true)
            .build()

        //se envia la notificacion
        notificationManager.notify(senderName.hashCode() + System.currentTimeMillis().toInt(), notification)
    }


    //cada usuario tenga un color diferente, cojo los que no se han usado
    fun getUserColor(userId: String): Color {
        return userColors.getOrPut(userId) {
            val usedColors = userColors.values.toSet()
            val unusedColors = availableColors.filterNot { it in usedColors }

            // Usa color sin usar o  primero disponible
            unusedColors.randomOrNull() ?: availableColors.random()
        }
    }
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}

