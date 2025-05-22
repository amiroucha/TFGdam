package com.example.tfg_1.model

data class ChatMessageModel (
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp:Long=System.currentTimeMillis(),
)