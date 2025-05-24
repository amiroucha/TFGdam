package com.example.tfg_1.ui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
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
        val chatItems = messages.withDateLabels()


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
            items(chatItems) { item ->
                when (item) {
                    is ChatItem.DateLabel -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item.label, color = Color.Gray)
                        }
                    }
                    is ChatItem.Message -> {
                        val isCurrentUser = item.message.senderId == currentUserId
                        MessageBox(message = item.message, isCurrentUser = isCurrentUser, viewModel = viewModel)
                    }
                }
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
                placeholder = { Text(stringResource(R.string.escribe_un_mensaje)) },
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
                Box( //para poder ponerle un fondo
                    modifier = Modifier
                        .background(colorResource(id = R.color.greenOscuro), shape = CircleShape)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = stringResource(R.string.escribe_un_mensaje),
                        tint = colorResource(id = R.color.white)
                    )
                }
            }
        }
    }
}



@Composable
fun MessageBox(
    message: ChatMessageModel,
    isCurrentUser: Boolean,
    viewModel: ChatViewModel
) {
    val backgroundColor = if (isCurrentUser) {
        colorResource(id = R.color.lilaChat) // color para el usuario actual
    } else {
        //getColorForUser(message.senderId)
        viewModel.getUserColor(message.senderId)
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

//hora/minutos
fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}




sealed class ChatItem {
    data class Message(val message: ChatMessageModel) : ChatItem()
    data class DateLabel(val label: String) : ChatItem()
}
@Composable
fun getDateLabel(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val oneDay = 24 * 60 * 60 * 1000L
    val diff = now - timestamp


    return when {
        diff < oneDay && isSameDay(now, timestamp) ->  stringResource(id = R.string.hoy)
        diff < 2 * oneDay && isSameDay(now - oneDay, timestamp) -> stringResource(id = R.string.ayer)
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
fun List<ChatMessageModel>.withDateLabels(): List<ChatItem> {
    val result = mutableListOf<ChatItem>()
    var lastDate: String? = null

    this.forEach { message ->
        val dateLabel = getDateLabel(message.timestamp)  // Esta función la defines tú (más abajo te ayudo)

        if (dateLabel != lastDate) {
            result.add(ChatItem.DateLabel(dateLabel))
            lastDate = dateLabel
        }
        result.add(ChatItem.Message(message))
    }
    return result
}


fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}