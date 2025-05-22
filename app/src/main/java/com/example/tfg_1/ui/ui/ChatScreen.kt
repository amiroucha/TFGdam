package com.example.tfg_1.ui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.tfg_1.R
import com.example.tfg_1.model.ChatMessageModel
import com.example.tfg_1.viewModel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages = viewModel.messages
    var newMessage by remember { mutableStateOf("") }
    val currentUserId = viewModel.currentUserId
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadChat()
    }

    // Auto-scroll al último mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }


    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (messageList, chatBox) = createRefs()

        LazyColumn(
            state = listState,
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
                MessageBox(message = message, isCurrentUser = isCurrentUser)
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
fun MessageBox(message: ChatMessageModel, isCurrentUser: Boolean) {
    val backgroundColor = if (isCurrentUser) {
        colorResource(id = R.color.lilaChat) // color para el usuario actual
    } else {
        getColorForUser(message.senderId)
    }

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
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
                    fontSize = 15.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(id = R.color.black),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = message.text,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Hora en esquina inferior derecha
            Text(
                text = formatTime(message.timestamp),
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.End)
            )
        }
    }
}


fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun getColorForUser(senderId: String): Color {
    return when (senderId.hashCode() % 5) {
        0 -> colorResource(id = R.color.naranjaChat)
        1 -> colorResource(id = R.color.azulChat)
        2 -> colorResource(id = R.color.verdeChat)
        3 -> colorResource(id = R.color.rosaChat)
        else -> colorResource(id = R.color.amarilloChat)
    }
}
