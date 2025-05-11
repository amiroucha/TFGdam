package com.example.tfg_1.ui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_1.R
import com.example.tfg_1.viewModel.HomeViewModel


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeBody(
        name            = "",
        address         = "",
        code            = "",
        onNameChange    = {},
        onAddressChange = {},
        onCodeChange    = {},
        onCreate        = {},
        onJoin          = {},
        modifier        = Modifier.fillMaxSize()
    )
}

@Composable
fun HomeBody(
    name: String,
    address: String,
    code: String,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.greyBackground))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "¡¡Bienvenido!! \nAntes de seguir, configura tu hogar.",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(text ="Nombre del hogar",
                fontSize = 18.sp) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text(text="Dirección (opcional)",
                fontSize = 18.sp) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onCreate,
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                colorResource(id = R.color.blue),
                colorResource(id = R.color.black),
                colorResource(id = R.color.blue), //color de fondo
                colorResource(id = R.color.black)//color del texto
            ),
        ) {
            Text(text = "Crear hogar nuevo",
                fontSize = 18.sp)
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            label = { Text(text = "Código de hogar existente",
                fontSize = 18.sp) },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onJoin,
            enabled = code.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                colorResource(id = R.color.black) ,
                colorResource(id = R.color.black),
                colorResource(id = R.color.green), //color de fondo
                colorResource(id = R.color.black)//color del texto
            ),
        ) {
            Text(text = "Unirme a hogar",
                fontSize = 18.sp)
        }
    }
}

// 2) Wrapper que conecta el ViewModel con la UI
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    // 2.1. Recogemos los estados
    val name    by viewModel.name.collectAsState()
    val address by viewModel.address.collectAsState()
    val code    by viewModel.code.collectAsState()

    // 2.2. Llamamos al composable puro con lambdas que invocan al ViewModel
    HomeBody(
        name           = name,
        address        = address,
        code           = code,
        onNameChange   = viewModel::changeName,
        onAddressChange= viewModel::changeAdress,
        onCodeChange   = viewModel::actCode,
        onCreate       = viewModel::createHome,
        onJoin         = viewModel::joinHome,
        modifier       = modifier
    )
}

