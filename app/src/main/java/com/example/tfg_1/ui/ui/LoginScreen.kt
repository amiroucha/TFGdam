package com.example.tfg_1.ui.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tfg_1.R
import com.example.tfg_1.navigation.Screens
import com.example.tfg_1.viewModel.LoginViewModel


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    val viewModel = LoginViewModel()
    LoginScreen(viewModel = viewModel, navController)
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    navController: NavController
) {
    Box(
        Modifier
            .fillMaxSize()
            //.padding(5.dp)
            .background( color = colorResource(id = R.color.greyBackground))
    ) {
        LoginBody(Modifier.align(Alignment.Center), viewModel, navController)
    }
}

@Composable
fun LoginBody(modifier: Modifier, viewModel: LoginViewModel, navController: NavController) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }else{
        Column(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)
                .fillMaxWidth().padding(16.dp))
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

                    LogoHeader(Modifier) // Ajusta el tamaño del logo según sea necesario
                }
            }


            TituloLogin(Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.padding(5.dp))
            EmailField(email) { viewModel.onLoginChanges(it, password) }

            Spacer(modifier = Modifier.padding(4.dp))
            PasswordField(password) { viewModel.onLoginChanges(email, it) }

            ForgotPassword(Modifier.align(Alignment.End))
            Spacer(modifier = Modifier.padding(16.dp))

            Column(modifier = Modifier.align(Alignment.End).padding(end = 20.dp))
            {
                LoginButton(navController)

                Spacer(modifier = Modifier.padding(16.dp))
                GoogleButton()
            }
            Column(modifier = Modifier.align(Alignment.CenterHorizontally)){
                Spacer(modifier = Modifier.padding(16.dp))
                Text(text = "¿ No tienes cuenta ?",
                    fontSize = 20.sp,
                    color = colorResource(id = R.color.black),
                    modifier = Modifier.padding(10.dp).align(Alignment.CenterHorizontally)
                )
                RegisterButton(navController)

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
                .padding(10.dp,10.dp)
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
fun PasswordField(password: String, onTextFieldChanged: (String) -> Unit) {
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
@Composable
fun ForgotPassword(modifier: Modifier) {
    Text(
        text = "Olvidaste la contraseña?",
        modifier = modifier
            .clickable { }
            .padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 16.dp),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = colorResource(id = R.color.black),
        textDecoration = TextDecoration.Underline
    )
}
@Composable
fun LoginButton(navController: NavController) {
    Button(
        onClick = { navController.navigate(Screens.Home.route) },
        modifier = Modifier
            .height(48.dp).width(250.dp),
        colors = ButtonDefaults.buttonColors(
            colorResource(id= R.color.brown), //color contenido
        ), enabled = true
    ) {
        Text(text = "Iniciar sesión",
            fontSize = 20.sp,
            color = colorResource(id = R.color.black),
            )
    }
}

@Composable
fun GoogleButton() {
    Button(
        onClick = { },
        modifier = Modifier
            .height(48.dp).width(250.dp),
        colors = ButtonDefaults.buttonColors(
            Color(0xFFFF4303),//boton habilitado
            Color(0xFFF78058),//boton desabilitado
            colorResource(id = R.color.blue), //color contenido
            disabledContentColor = Color.White
        ), enabled = true
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


@Composable
fun RegisterButton(navController: NavController) {
    Button(
        onClick = {  navController.navigate(Screens.Register.route)},
        modifier = Modifier
            .height(48.dp).width(250.dp),
        colors = ButtonDefaults.buttonColors(
            colorResource(id = R.color.brownRegister),
            colorResource(id = R.color.brownRegister),
            colorResource(id = R.color.brownRegister),
            disabledContentColor = Color.White
        ), enabled = true
    ) {

        Spacer(modifier = Modifier.width(8.dp)) // Espacio entre icono y texto
        Text(text = "Registrate aquí",
            fontSize = 20.sp,
            color = colorResource(id = R.color.black)
        )
    }
}

