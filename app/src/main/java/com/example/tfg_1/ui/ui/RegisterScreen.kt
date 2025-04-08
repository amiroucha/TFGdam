package com.example.tfg_1.ui.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_1.R
import com.example.tfg_1.viewModel.RegisterViewModel
import kotlinx.coroutines.launch


@Preview(showBackground = true)
@Composable
fun registerScreenPreview() {
    val viewModel = RegisterViewModel()
    RegisterScreen(viewModel = viewModel)
}

@Composable
fun RegisterScreen(viewModel: RegisterViewModel) {
    Box(
        Modifier
            .fillMaxSize()
            //.padding(5.dp)
            .background( color = colorResource(id = R.color.greyBackground))
    ) {
        Register(Modifier.align(Alignment.Center), viewModel)
    }
}

@Composable
fun Register (modifier: Modifier, viewModel: RegisterViewModel) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val password2 by viewModel.password2.collectAsState()
    val isRegisterEnabled by viewModel.registerEnable.collectAsState()
    val isLoading by viewModel.isLoadingR.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }else{
        Column(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)
                .fillMaxWidth().padding(top = 10.dp, bottom= 16.dp, end = 16.dp, start = 16.dp)
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically // Alinea verticalmente al centro
                ) {
                    Text(
                        text = "FLOWHOME",
                        modifier = Modifier.padding(start = 8.dp, end = 15.dp), // Espaciado a la izquierda
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    LogoHeaderReg(Modifier) // Ajusta el tamaño del logo según sea necesario
                }
            }


            TituloRegister(Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.padding(5.dp))
            EmailFieldReg(email) { viewModel.onLoginChanges(it, password, password2) }
            Spacer(modifier = Modifier.padding(4.dp))
            PasswordField(password) { viewModel.onLoginChanges(email, it, password2) }
            Spacer(modifier = Modifier.padding(4.dp))
            PasswordFieldReg2(password2) { viewModel.onLoginChanges(email,password, it) }
            Spacer(modifier = Modifier.padding(10.dp))
            Column(modifier = Modifier.align(Alignment.End).padding(end = 20.dp))
            {
                RegisterButtonReg(isRegisterEnabled) {
                    coroutineScope.launch {

                    }
                }
            }

            Spacer(modifier = Modifier.padding(20.dp))
            Column(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 25.dp))
            {
                backButton(isRegisterEnabled) {

                }
            }
        }

    }


}
//Imagen Logo------------------------------------------------
@Composable
fun LogoHeaderReg(modifier:Modifier)
{
        Image(
            painter = painterResource(id= R.drawable.logotfg),
            contentDescription = "Hogar",
            modifier = modifier
                .padding(10.dp,10.dp)
                .size(120.dp)
                .clip(CircleShape)
            // .border(7.dp, color = Color.Black)
        )

}

//Titulo---------------------------------------------
@Composable
fun TituloRegister(modifier:Modifier){
    Text( text = "REGISTRO" ,
        modifier = modifier
            .padding(top = 0.dp, bottom= 5.dp, end = 5.dp, start = 5.dp),
        fontSize = 50.sp,
        maxLines = 1, // Solo permite 1 línea
        fontWeight = FontWeight.Bold
    )
}
//email ----------------------------------------------------------------
@Composable
fun EmailFieldReg(email: String, onTextFieldChanged: (String) -> Unit) {
    Row(modifier = Modifier){
        TextField(
            value = email, onValueChange = { onTextFieldChanged(it) },
            modifier = Modifier
                .height(90.dp)
                .weight(1f)
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 10.dp)
                .border(2.dp, Color.Black),
            placeholder = { Text(text = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            maxLines = 1,
            colors = TextFieldDefaults.colors(
                focusedTextColor= colorResource(id = R.color.white),//color del texto
                focusedContainerColor = colorResource(id = R.color.black),
                unfocusedTextColor= colorResource(id = R.color.black),
                unfocusedContainerColor = colorResource(id = R.color.white),
            )
        )
    }
}
//Contraseña------------------------------------------------
@Composable
fun PasswordFieldReg(password: String, onTextFieldChanged: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) } //mostrar/ocultar contraseña
    Row(modifier = Modifier)
    {
        TextField(
            value = password, onValueChange = { onTextFieldChanged(it) },
            placeholder = { Text(text = "Contraseña") },
            modifier = Modifier
                .height(90.dp)
                .weight(1f)
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 10.dp)
                .border(2.dp, Color.Black),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            maxLines = 1,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), // Muestra u oculta la contraseña
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Mostrar contraseña")
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor= colorResource(id = R.color.white),//color del texto
                focusedContainerColor = colorResource(id = R.color.black),
                unfocusedTextColor= colorResource(id = R.color.black),
                unfocusedContainerColor = colorResource(id = R.color.white),
            ),
        )

    }
}

//Contraseña  2 ------------------------------------------------
@Composable
fun PasswordFieldReg2(password: String, onTextFieldChanged: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) } //mostrar/ocultar contraseña
    Row(modifier = Modifier)
    {
        TextField(
            value = password, onValueChange = { onTextFieldChanged(it) },
            placeholder = { Text(text = "Repite tu contraseña") },
            modifier = Modifier
                .height(90.dp)
                .weight(1f)
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 10.dp)
                .border(2.dp, Color.Black),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            maxLines = 1,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), // Muestra u oculta la contraseña
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Mostrar contraseña")
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor= colorResource(id = R.color.white),//color del texto
                focusedContainerColor = colorResource(id = R.color.black),
                unfocusedTextColor= colorResource(id = R.color.black),
                unfocusedContainerColor = colorResource(id = R.color.white),
            ),
        )

    }
}

@Composable
fun RegisterButtonReg(loginEnable: Boolean, onLoginSelected: () -> Unit) {
    Button(
        onClick = { onLoginSelected() },
        modifier = Modifier
            .height(48.dp).width(250.dp),
        colors = ButtonDefaults.buttonColors(
            Color(0xFFFF4303),//boton habilitado
            Color(0xFFF78058),//boton desabilitado
            colorResource(id= R.color.green), //color contenido
            disabledContentColor = Color.White
        ), enabled = loginEnable
    ) {
        Text(text = "Registrarse",
            fontSize = 20.sp,
            color = colorResource(id = R.color.black),
        )
    }
}

@Composable
fun backButton(loginEnable: Boolean, onLoginSelected: () -> Unit) {
    Button(
        onClick = { onLoginSelected() },
        modifier = Modifier
            .height(48.dp).width(250.dp),
        colors = ButtonDefaults.buttonColors(
            Color(0xFFFF4303),//boton habilitado
            Color(0xFFF78058),//boton desabilitado
            colorResource(id= R.color.red), //color contenido
            disabledContentColor = Color.White
        ), enabled = loginEnable
    ) {
        Text(text = "Cancelar",
            fontSize = 20.sp,
            color = colorResource(id = R.color.black),
        )
    }
}
/*
* Boton registro
* botoon cancelar
*
*
*
* */

