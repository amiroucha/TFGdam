package com.example.tfg_1.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
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

    fun loadChat() {
        viewModelScope.launch {
            currentUserId = userRepository.getCurrentUserId()
            currentUserName = userRepository.getCurrentUserName()
            val homeId = userRepository.getCurrentUserHomeId()

            listenerRegistration?.remove() // Detener escucha anterior
            listenChat(homeId)
        }
    }

    private fun listenChat(homeId: String) {
        listenerRegistration?.remove() // Elimina anterior si existe para evitar duplicaciones

        listenerRegistration = db.collection("hogares")
            .document(homeId)
            .collection("mensajes")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    _messages.clear()
                    for (doc in it.documents) {
                        try {
                            val msg = doc.toObject(ChatMessageModel::class.java)
                            if (msg != null) {
                                _messages.add(msg)
                            }
                        } catch (e: Exception) {
                            Log.e("ChatViewModel", "Error parsing message document: ${doc.id}", e)
                        }
                    }
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

