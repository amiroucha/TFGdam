package com.example.tfg_1.ui.ui

import android.content.*
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tfg_1.R
import com.example.tfg_1.navigation.Screens
import com.example.tfg_1.repositories.UserRepository
import com.example.tfg_1.viewModel.SettingsViewModel
import com.example.tfg_1.viewModel.ThemeViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel()

    val homeId   by viewModel.homeId.collectAsState()
    val members  by viewModel.members.collectAsState()
    val darkMode by themeViewModel.isDarkTheme.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(16.dp)

    LaunchedEffect(uiState) {
        when (uiState) {
            SettingsViewModel.SettingsUiState.LeftHome,
            SettingsViewModel.SettingsUiState.NoHome -> {
                // el usuario ya no tiene hogar → llévalo a Home
                navController.navigate(Screens.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is SettingsViewModel.SettingsUiState.Error -> {
                val msg = (uiState as SettingsViewModel.SettingsUiState.Error).msg
                Toast.makeText(context, msg ?: "Error", Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

       // Modo oscuro -----------------------------------------------
        Card(
            shape = cardShape,
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.modo_oscuro),
                    style = MaterialTheme.typography.titleMedium,
                    color= MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = darkMode,
                    onCheckedChange = { themeViewModel.toggleTheme(it) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        val userRepository = remember { UserRepository() }
        var homeName by remember { mutableStateOf("") }
        // cargar datos del usuario y hogar
        LaunchedEffect(Unit) {
            homeName = userRepository.getCurrentHomeName()
        }
        // Miembros del hogar -------------------------------------------
        Card(
            shape = cardShape,
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier
                .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(15.dp))
                .padding(20.dp))
            {
                Text(
                    text = stringResource(R.string.hogarNombre, homeName),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    color= MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp)

                )
                Text(
                    text = stringResource(R.string.miembrosHogar),
                    style = MaterialTheme.typography.titleMedium,
                    color= MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(5.dp)
                )
                members.forEach {
                    Text(
                        text = "• ${it.name} (${it.email})",
                        style = MaterialTheme.typography.bodyMedium,
                        color= MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

       // Añade nuevos miembros -------------------------------------------
        Card(
            shape = cardShape,
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(15.dp))
                .padding(20.dp)) {
                Text(
                    text = stringResource(R.string.aniadeMiembros),
                    style = MaterialTheme.typography.titleMedium,
                    color= MaterialTheme.colorScheme.onBackground

                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = homeId,
                    style = MaterialTheme.typography.bodyLarge,
                    color= MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { copyHomeId(context, homeId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.purple_500),
                            disabledContainerColor = colorResource(id = R.color.purple_500),
                        )
                    ) {
                        Text(text = stringResource(R.string.copiar_codigo),
                            color= MaterialTheme.colorScheme.onPrimary)
                    }
                    Button(onClick = { shareWhatsApp(context, homeId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.purple_500),
                            disabledContainerColor = colorResource(id = R.color.purple_500),
                        )
                    ) {
                        Text(text = stringResource(R.string.compartir_en_whatsapp),
                            color= MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        //Cambiar hogar--------------------------------------
        Card(
            shape = cardShape,
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(15.dp))
                .padding(20.dp)) {
                Text(
                    text = stringResource(R.string.unirse_a_un_hogar),
                    style = MaterialTheme.typography.titleMedium,
                    color= MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.red),
                        contentColor   = colorResource(id = R.color.white)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cambiar_de_hogar))
                }
            }
        }
    }

    //confirmacion de cambio de hogar------------------------------------------
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.cambiar_de_hogar)) },
            text  = { Text(stringResource(R.string.perderHogar)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        //borrar idhome
                        viewModel.clearCurrentHome()
                    }
                ) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}




fun copyHomeId(context: Context, code: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("homeCode", code)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, context.getString(R.string.codigo_copiado), Toast.LENGTH_SHORT).show()
}

fun shareWhatsApp(context: Context, code: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.unete_hogar, code))
        type = "text/plain"
        setPackage("com.whatsapp")
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.whatsapp_no_instalado), Toast.LENGTH_SHORT).show()
    }
}
