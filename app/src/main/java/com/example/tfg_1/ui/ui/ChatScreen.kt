package com.example.tfg_1.ui.ui

import android.content.Context
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
import com.example.tfg_1.notifications.PreferenceMnger
import com.example.tfg_1.viewModel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(viewModel: ChatViewModel, searchText: String) {
    val context = LocalContext.current

    val messages = viewModel.filteredMessages //lista de mensajes filtrados o no
    //mensajes que se van escribiendo
    var newMessage by remember { mutableStateOf("") }
    //distinguir quien ha enviado el mensaje
    val currentUserId = viewModel.currentUserId
    //posición del scroll de la lista, que no se quede en el 1 mensaje
    val listState = rememberLazyListState()

    val isLoading by viewModel.isLoading

    //cargar la informacion (mensajes)
    LaunchedEffect(Unit) {
        viewModel.init(context)//para notificaciones
        viewModel.loadChat(context)
    }

    // Auto-scroll al último mensaje cada vez que hay ++ mensaje
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    //filtro busqueda del textp
    LaunchedEffect(searchText) {
        viewModel.updateSearchQuery(searchText)
    }

    //posicionar lista y caja de texto de manera flexible
    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (messageList, chatBox, loadingSpinner) = createRefs()

        if (isLoading) {
            // Mostrar spinner
            CircularProgressIndicator(
                modifier = Modifier.constrainAs(loadingSpinner) {
                    centerTo(parent)
                }
            )
        } else {
            //convertir lista de mensajes lista de elementos para etiquetas de fecha cuando cambia dia
            //porque dentro de items no puede haber ootro item
            val chatItems = viewModel.filteredMessages.withDateLabels()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .constrainAs(messageList) {
                        //constraints
                        top.linkTo(parent.top)
                        bottom.linkTo(chatBox.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        height = Dimension.fillToConstraints
                    },
                reverseLayout = false //scroll de arriba a abajo
            ) {
                //2 opciones : mensaje / fecha
                items(chatItems) { item ->
                    when (item) {
                        //fecha centrada y con un color gris
                        is ChatItem.DateLabel -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item.label,
                                    color = colorResource(id = R.color.black))
                            }
                        }
                        //mensaje =  cuadro de mensaje
                        is ChatItem.Message -> {
                            //cpmpruebo si mensaje es del usuario act , para posicionamiento y colr
                            val isCurrentUser = item.message.senderId == currentUserId
                            MessageBox(message = item.message, isCurrentUser = isCurrentUser, viewModel = viewModel)
                        }
                    }
                }
            }
            //textInput y btn enviar
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
                    modifier = Modifier.weight(1f), //ocupa el ancho - el btn
                    placeholder = { Text(stringResource(R.string.escribe_un_mensaje)) },
                    //accion enviar desde el teclado
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (newMessage.isNotBlank()) {
                                // Enviar = mensaje no vacío
                                viewModel.sendMessage(newMessage)
                                newMessage = "" //limpio mi bandeja
                            }
                        }
                    )
                )
                // Botón para enviar mensaje
                IconButton(
                    onClick = {
                        if (newMessage.isNotBlank()) {
                            viewModel.sendMessage(newMessage)
                            newMessage = ""// Limpiar campo
                        }
                    }
                ) {
                    Box( //para poder ponerle un fondo e icono circular
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
}


//cada cja de mensaje
@Composable
fun MessageBox(
    message: ChatMessageModel,
    isCurrentUser: Boolean,
    viewModel: ChatViewModel
) {
    // Color de fondo del mensaje según si es del usuario actual o no
    val backgroundColor = if (isCurrentUser) {
        colorResource(id = R.color.lilaChat) // color para el usuario actual
    } else {
        // Color asignado a otros usuarios, de forma aleatoria
        viewModel.getUserColor(message.senderId)
    }
    // Alineación de la fila, derecha / izquierda
    val alignment = if (isCurrentUser) Arrangement.End else Arrangement.Start
    // Forma de la burbuja del mensaje
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
                .widthIn(max = 290.dp) // Limita el ancho del mensaje
        ) {
            // Si mensaje != usuario actual, = nombre arriba en negrita
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
            // Texto del mensaje
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

//funcion para pasar timespam -> hora/minutos
fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// devuelve la fecha según timestamp y día actual
@Composable
fun getDateLabel(timestamp: Long): String {
    val now = System.currentTimeMillis() //hoy en milisegundos
    val oneDay = 24 * 60 * 60 * 1000L// milisegundos en un día
    //diferencia milisegundos entre ahora y de cuando se envio mensaje
    val diff = now - timestamp

// compara si el mensaje es de hoy, ayer o de otro día
// y devuelve la cadena de fecha que es
    return when {
        //si la diferencia es menor , sigue siendo hoy, hace - de 24h
        diff < oneDay && isSameDay(now, timestamp) ->  stringResource(id = R.string.hoy)
        //- de 48h de diferncia
        //now - oneDay = mismo instante que hace 24h y timespam iguales?
        diff < 2 * oneDay && isSameDay(now - oneDay, timestamp) -> stringResource(id = R.string.ayer)
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

// lista de mensajes + etiquetas de date
@Composable
fun List<ChatMessageModel>.withDateLabels(): List<ChatItem> {
    val result = mutableListOf<ChatItem>()
    var lastDate: String? = null

    this.forEach { message ->
        // etiqueta de fecha correspondiente al mensaje actual
        val dateLabel = getDateLabel(message.timestamp)
        // Si etiqueta de fecha cambia añadirla a la lista
        if (dateLabel != lastDate) {
            result.add(ChatItem.DateLabel(dateLabel))
            lastDate = dateLabel
        }
        // añadir el mensaje
        result.add(ChatItem.Message(message))
    }
    return result
}

// comprobar si dos timestamps pertenecen al mismo día
fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

//para poder mostrar la fecha o si es hoy o syer ....
sealed class ChatItem {
    data class Message(val message: ChatMessageModel) : ChatItem()
    data class DateLabel(val label: String) : ChatItem()
}