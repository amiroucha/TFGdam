package com.example.tfg_1.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_1.model.ChatMessageModel
import com.example.tfg_1.repositories.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val db = Firebase.firestore

    private val _messages = mutableStateListOf<ChatMessageModel>()
    val messages: List<ChatMessageModel> = _messages

    private var listenerRegistration: ListenerRegistration? = null
    var currentUserId: String = ""
    private var currentUserName: String = ""

    var isLoading by mutableStateOf(true)
        private set

    fun loadChat() {
        viewModelScope.launch {
            isLoading = true
            currentUserId = userRepository.getCurrentUserId()
            currentUserName = userRepository.getCurrentUserName()
            val homeId = userRepository.getCurrentUserHomeId()

            listenerRegistration?.remove() // Detener escucha anterior
            listenChat(homeId)
        }
    }

    private fun listenChat(homeId: String) {
        listenerRegistration?.remove()

        listenerRegistration = db.collection("hogares")
            .document(homeId)
            .collection("mensajes")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _messages.clear()
                    for (doc in snapshot.documents) {
                        try {
                            val msg = doc.toObject(ChatMessageModel::class.java)
                            if (msg != null) {
                                _messages.add(msg)
                            }
                        } catch (e: Exception) {
                            Log.e("ChatViewModel", "Error parsing message document: ${doc.id}", e)
                        }
                    }
                    isLoading = false //datos cargados
                } else {
                    // En caso de error o snapshot nulo, no dejar carga inf
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

            Firebase.firestore
                .collection("hogares")
                .document(homeId)
                .collection("mensajes")
                .add(message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}

