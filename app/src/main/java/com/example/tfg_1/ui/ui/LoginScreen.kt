package com.example.tfg_1.ui.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_1.R
import com.example.tfg_1.viewModel.LoginViewModel
import kotlinx.coroutines.launch


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val viewModel = LoginViewModel()
    LoginScreen(viewModel = viewModel)
}

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    Box(
        Modifier
            .fillMaxSize()
            //.padding(5.dp)
            .background( color = colorResource(id = R.color.greyBackground))
    ) {
        Login(Modifier.align(Alignment.Center), viewModel)
    }
}

@Composable
fun Login (modifier: Modifier, viewModel: LoginViewModel) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoginEnabled by viewModel.loginEnable.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }else{
        Column(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth()) {
                Text( text = "FLOWHOME" ,
                    modifier = modifier
                        .align(Alignment.CenterStart)
                        .padding(5.dp),
                    fontSize = 40.sp,
                    maxLines = 1, // Solo permite 1 línea
                    fontWeight = FontWeight.Bold
                )
                LogoHeader(Modifier.align(Alignment.TopEnd))
            }


            TituloLogin(Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.padding(5.dp))
            EmailField(email) { viewModel.onLoginChanges(it, password) }
            Spacer(modifier = Modifier.padding(4.dp))
            PasswordField(password) { viewModel.onLoginChanges(email, it) }
            Spacer(modifier = Modifier.padding(8.dp))
            ForgotPassword(Modifier.align(Alignment.End))
            Spacer(modifier = Modifier.padding(16.dp))
            LoginButton(isLoginEnabled) {
                coroutineScope.launch {
                    viewModel.onLoginSelected()
                }
            }
            Spacer(modifier = Modifier.padding(16.dp))
            GoogleButton(isLoginEnabled) {
                coroutineScope.launch {
                    viewModel.onLoginSelected()
                }
            }

        }

    }


}
//Imagen Logo------------------------------------------------
@Composable
fun LogoHeader(modifier:Modifier)
{
        Image(
            painter = painterResource(id= R.drawable.logotfg),
            contentDescription = "Hogar",
            modifier = modifier
                .padding(10.dp,20.dp)
                .size(120.dp)
                .clip(CircleShape)
            // .border(7.dp, color = Color.Black)
        )

}

//Titulo---------------------------------------------
@Composable
fun TituloLogin(modifier:Modifier){
    Text( text = "Iniciar Sesión" ,
        modifier = modifier
            .padding(10.dp),
        fontSize = 40.sp,
        maxLines = 1, // Solo permite 1 línea
        fontWeight = FontWeight.Bold
    )
}

//Email------------------------------------------------
@Composable
fun EmailField(email: String, onTextFieldChanged: (String) -> Unit) {
    Row(modifier = Modifier){
        TextField(
            value = email, onValueChange = { onTextFieldChanged(it) },
            modifier = Modifier
                .height(90.dp)
                .weight(1f)
                .padding(16.dp),
            placeholder = { Text(text = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            maxLines = 1,
            colors = TextFieldDefaults.colors(
                Color(0xFF636262),//color del texto
                Color(0xFFDEDDDD),//color del fondo del input
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}
//Contraseña------------------------------------------------
@Composable
fun PasswordField(password: String, onTextFieldChanged: (String) -> Unit) {
    Row(modifier = Modifier)
    {
        TextField(
            value = password, onValueChange = { onTextFieldChanged(it) },
            placeholder = { Text(text = "Contraseña") },
            modifier = Modifier
                .height(90.dp)
                .weight(1f)
                .padding(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            maxLines = 1,
            colors = TextFieldDefaults.colors(
                Color(0xFF636262),//color texto
                Color(0xFFDEDDDD),//color fondo
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

    }
}
@Composable
fun ForgotPassword(modifier: Modifier) {
    Text(
        text = "Olvidaste la contraseña?",
        modifier = modifier
            .clickable { }
            .padding(10.dp),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
}
@Composable
fun LoginButton(loginEnable: Boolean, onLoginSelected: () -> Unit) {
    Button(
        onClick = { onLoginSelected() },
        modifier = Modifier
            .height(48.dp).width(200.dp),
        colors = ButtonDefaults.buttonColors(
            Color(0xFFFF4303),//boton habilitado
            Color(0xFFF78058),//boton desabilitado
            Color.Black, //color contenido
            disabledContentColor = Color.White
        ), enabled = loginEnable
    ) {
        Text(text = "Iniciar sesión",
            fontSize = 20.sp,
            )
    }
}

@Composable
fun GoogleButton(loginEnable: Boolean, onLoginSelected: () -> Unit) {
    Button(
        onClick = { onLoginSelected() },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp).
            width(200.dp),
        colors = ButtonDefaults.buttonColors(
            Color(0xFFFF4303),//boton habilitado
            Color(0xFFF78058),//boton desabilitado
            colorResource(id = R.color.blue), //color contenido
            disabledContentColor = Color.White
        ), enabled = loginEnable
    ) {

        Text(text = "Iniciar con Google",
                fontSize = 20.sp,
                color = colorResource(id = R.color.black)
        )
        Spacer(modifier = Modifier.width(8.dp)) // Espacio entre icono y texto
        Icon(
            painter = painterResource(id = R.drawable.ic_google), // Ícono de Google
            contentDescription = "Google Icon",
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified // Mantiene los colores originales del icono
        )
    }
}



