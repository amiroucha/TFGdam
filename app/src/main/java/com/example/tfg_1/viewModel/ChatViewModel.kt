package com.example.tfg_1.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_1.model.ChatMessageModel
import com.example.tfg_1.repositories.UserRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _messages = mutableStateListOf<ChatMessageModel>()
    val messages: List<ChatMessageModel> = _messages

    private var listenerRegistration: ListenerRegistration? = null
    var currentUserId: String = ""
    private var currentUserName: String = ""

    private val userColors = mutableMapOf<String, androidx.compose.ui.graphics.Color>()
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

    private var isLoading by mutableStateOf(true)

    fun loadChat() {
        viewModelScope.launch {
            isLoading = true
            currentUserId = userRepository.getCurrentUserId()
            currentUserName = userRepository.getCurrentUserName()
            val homeId = userRepository.getCurrentUserHomeId()

            listenerRegistration?.remove() // Detener escucha anterior
            listenerRegistration = userRepository.escucharMensajes(homeId) { mensajes ->
                _messages.clear()
                _messages.addAll(mensajes)
                isLoading = false
            }
        }
    }

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
    //cada usuario tenga un color diferente
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

