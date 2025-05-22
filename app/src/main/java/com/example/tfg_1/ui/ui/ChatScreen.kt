package com.example.tfg_1.ui.ui

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.tfg_1.model.ChatMessageModel
import com.example.tfg_1.viewModel.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages = viewModel.messages
    var newMessage by remember { mutableStateOf("") }
    val currentUserId = viewModel.currentUserId

    LaunchedEffect(Unit) {
        viewModel.loadChat()
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (messageList, chatBox) = createRefs()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .constrainAs(messageList) {
                    top.linkTo(parent.top)
                    bottom.linkTo(chatBox.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    height = Dimension.fillToConstraints
                },
            reverseLayout = false
        ) {
            items(messages) { message ->
                val isCurrentUser = message.senderId == currentUserId
                ChatMessageBubble(message = message, isCurrentUser = isCurrentUser)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .constrainAs(chatBox) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje...") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (newMessage.isNotBlank()) {
                            viewModel.sendMessage(newMessage)
                            newMessage = ""
                        }
                    }
                )
            )
            IconButton(
                onClick = {
                    if (newMessage.isNotBlank()) {
                        viewModel.sendMessage(newMessage)
                        newMessage = ""
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Enviar")
            }
        }
    }
}



@Composable
fun ChatMessageBubble(message: ChatMessageModel, isCurrentUser: Boolean) {
    val backgroundColor = if (isCurrentUser) Color(0xFFD1F5C1) else Color(0xFFE0E0E0)
    val alignment = if (isCurrentUser) Arrangement.End else Arrangement.Start
    val bubbleShape = if (isCurrentUser) {
        RoundedCornerShape(12.dp, 0.dp, 12.dp, 12.dp)
    } else {
        RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp)
    }

    Row(
        horizontalArrangement = alignment,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(backgroundColor, shape = bubbleShape)
                .padding(12.dp)
                .widthIn(max = 300.dp)
        ) {
            if (!isCurrentUser) {
                Text(
                    text = message.senderName,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(text = message.text)
        }
    }
}











/*@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages = viewModel.messages
    var newMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadChat() // carga  datos del usuario actual
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                Text(
                    text = "${message.senderName}: ${message.text}",
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        ChatBox(newMessage, viewModel)
    }
}

@Composable
private fun ChatBox(newMessage: String, viewModel: ChatViewModel) {
    var newMessage1 = newMessage
    Row(modifier = Modifier.padding(8.dp)) {
        TextField(
            value = newMessage1,
            onValueChange = { newMessage1 = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escribe un mensaje...") }
        )

        Button(onClick = {
            if (newMessage1.isNotBlank()) {
                viewModel.sendMessage(newMessage1)
                newMessage1 = ""
            }
        }) {
            Text("Enviar")
        }
    }
}*/