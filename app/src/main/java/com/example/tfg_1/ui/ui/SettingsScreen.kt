package com.example.tfg_1.ui.ui

import android.content.*
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tfg_1.R
import com.example.tfg_1.viewModel.SettingsViewModel
import com.example.tfg_1.viewModel.ThemeViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel()

    val homeId by viewModel.homeId.collectAsState()
    val memebers by viewModel.members.collectAsState()

    val darkMode by themeViewModel.isDarkTheme.collectAsState()
    var language by rememberSaveable { mutableStateOf("es") }

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        //para el modo oscuro------------------------------------------
        Row (verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()

        ) {
            Text(text = stringResource(R.string.modo_oscuro), modifier = Modifier.weight(1f))
            Switch(checked = darkMode, onCheckedChange = {themeViewModel.toggleTheme(it) })
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        //miembros del hogar ------------------------------------------
        Text(text = stringResource(R.string.miembrosHogar), style = MaterialTheme.typography.titleMedium)
        memebers.forEach{
            Text(text = "\n-- ${it.name} - (${it.email})" )
        }

        Spacer(modifier = Modifier.height(16.dp))

        //hogar--------------------------------------------------------
        //añadir usuario
        Text(text = stringResource(R.string.aniadeMiembros) + homeId)
        Row {
            Button(onClick = { copyHomeId(context =context , code = homeId ) }) {
                Text(text = stringResource(R.string.copiar_codigo))
            }
            Button(onClick = { shareWhatsApp(context =context , code = homeId ) }) {
                Text(text = stringResource(R.string.compartir_en_whatsapp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        //cambiar de hogar-------------------------------------------
        Button(
            onClick = {showDialog = true},
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.red)
            )
        )
        {
            Text(text = stringResource(R.string.cambiar_de_hogar), color = colorResource(id = R.color.white))
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false }, 
                title = {Text(text = stringResource(R.string.cambiar_de_hogar))},
                text = { Text(text = stringResource(R.string.perderHogar))},
                confirmButton = {
                    TextButton(
                        onClick = { showDialog = false
                        navController.navigate("home"){
                            popUpTo(0) { inclusive = true }//borra historial de navegacion
                        }
                    }){
                        Text(text = "SI")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false}) {
                        Text(text = "Cancelar")
                    }
                }
            
            
            )
            
        }
    }

}


fun copyHomeId(context: Context, code: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("homeCode", code)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Código copiado al portapapeles", Toast.LENGTH_SHORT).show()
}

fun shareWhatsApp(context: Context, code: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Únete a mi hogar en la app con este código: $code")
        type = "text/plain"
        setPackage("com.whatsapp")
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
    }
}
