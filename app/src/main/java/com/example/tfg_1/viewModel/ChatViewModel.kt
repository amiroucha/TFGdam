package com.example.tfg_1.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}

