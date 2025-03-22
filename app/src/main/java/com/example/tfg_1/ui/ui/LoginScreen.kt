package com.example.tfg_1.ui.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tfg_1.viewModel.LoginViewModel
import androidx.compose.runtime.getValue


@Preview(showBackground = true)
@Composable
fun loginScreenPreview() {
    val viewModel = LoginViewModel()
    LoginScreen(viewModel = viewModel)
}

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        login(Modifier.align(Alignment.Center), viewModel)
    }
}

@Composable
fun login (modifier: Modifier, viewModel: LoginViewModel) {
   //val email: String by viewModel.email

}