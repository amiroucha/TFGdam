package com.example.tfg_1.ui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
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
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre del hogar") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Dirección (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onCreate,
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear hogar nuevo")
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            label = { Text("Código de hogar existente") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onJoin,
            enabled = code.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Unirme a hogar")
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

