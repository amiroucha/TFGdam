package com.example.tfg_1.ui.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tfg_1.R
import com.example.tfg_1.viewModel.RegisterViewModel
import java.util.Calendar
import androidx.compose.material.icons.filled.CalendarToday


@Preview(showBackground = true)
@Composable
fun registerScreenPreview() {
    val navController = rememberNavController()
    val viewModel = RegisterViewModel(navController)
    RegisterScreen(viewModel = viewModel/*, navController*/)
}

@Composable
fun RegisterScreen(viewModel: RegisterViewModel/*, navcontroller : NavController*/) {
    Box(
        Modifier
            .fillMaxSize()
            //.padding(5.dp)
            .background( color = colorResource(id = R.color.greyBackground))
    ) {
        RegisterBody(
            Modifier
                .align(Alignment.Center)
                .padding(10.dp),
            viewModel
            //navcontroller
        )
    }

}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterBody (modifier: Modifier, viewModel: RegisterViewModel) {
    val email by viewModel.email.collectAsState()
    val passwordR by viewModel.password.collectAsState()
    val password2 by viewModel.password2.collectAsState()
    val isLoading by viewModel.isLoadingR.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError1 by viewModel.passwordError.collectAsState()
    val passwordError2 by viewModel.passwordError2.collectAsState()
    val passwordSameError by viewModel.passwordsame.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }else{
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // ← Aquí agregamos el scroll
                .padding(top = 16.dp)
        ) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)
                .fillMaxWidth().padding(top = 5.dp, bottom= 16.dp, end = 16.dp, start = 16.dp)
            )
            {
                Row(
                    modifier = Modifier,
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
            EmailFieldReg(email, error = emailError) { viewModel.onLoginChanges(it, passwordR, password2) }
            Spacer(modifier = Modifier.padding(2.dp))
            PasswordFieldReg(passwordR, error = passwordError1) { viewModel.onLoginChanges(email, it, password2) }
            Spacer(modifier = Modifier.padding(4.dp))
            PasswordFieldReg2(password2, error = passwordError2)
            { viewModel.onLoginChanges(email,passwordR, it) }

            // error de contraseñas diferentes
            if (passwordSameError.isNotEmpty()) {
                Text(
                    text = passwordSameError,
                    color = colorResource(id = R.color.red),
                    fontSize = 15.sp,
                    modifier = Modifier.padding(start = 20.dp)
                )
            }

            FechaNacimientoField(viewModel)
            Spacer(modifier = Modifier.padding(15.dp))
            Column(modifier = Modifier.align(Alignment.End).padding(end = 20.dp))
            {
                RegisterButtonReg(viewModel)
            }

            Spacer(modifier = Modifier.padding(20.dp))
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
                .padding(start = 10.dp, end = 10.dp)
                .size(120.dp)
                .clip(CircleShape)
            // .border(7.dp, color = Color.Black)
        )

}

//Titulo---------------------------------------------
@Composable
fun TituloRegister(modifier:Modifier){
    Text( text = "REGISTRO" ,
        modifier = modifier.padding(top = 0.dp, bottom= 5.dp, end = 5.dp, start = 5.dp),
        fontSize = 40.sp,
        maxLines = 1, // Solo permite 1 línea
        fontWeight = FontWeight.Bold
    )
}
//email ----------------------------------------------------------------
@Composable
fun EmailFieldReg(email: String,error: String?, onTextFieldChanged: (String) -> Unit) {
    Column (modifier = Modifier.padding(top = 5.dp, bottom=1.dp, end = 20.dp, start = 20.dp )) {
        TextField(
            value = email,
            onValueChange = { onTextFieldChanged(it) },//actualiza rl valor
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .border(2.dp, Color.Black),
            placeholder = { Text(text = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = error != null,
            colors = TextFieldDefaults.colors(
                focusedTextColor = colorResource(id = R.color.white),
                unfocusedTextColor = colorResource(id = R.color.white),
                focusedContainerColor = colorResource(id = R.color.white),
                unfocusedContainerColor = colorResource(id = R.color.white),
                disabledContainerColor = colorResource(id = R.color.white),
                errorContainerColor = colorResource(id = R.color.white),
                errorIndicatorColor = colorResource(id = R.color.red)
            )
        )
        error?.let { //si el error!=null -> hay error , entonces:
            Text(
                text = it,//it es el valor del error (no null)
                color = colorResource(id = R.color.red),
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
//Contraseña 1------------------------------------------------
@Composable
fun PasswordFieldReg(password: String, error: String?, onTextFieldChanged: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) } //mostrar/ocultar contraseña
    Column (modifier = Modifier.padding(top = 1.dp, bottom=1.dp, end = 20.dp, start = 20.dp ))
    {
        TextField(
            value = password,
            onValueChange = { onTextFieldChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .border(2.dp, Color.Black),
            placeholder = { Text("Contraseña") },
            singleLine = true,
            isError = error != null,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image,
                        contentDescription = "Mostrar contraseña",
                        tint = colorResource(id = R.color.black) )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = colorResource(id = R.color.black),
                unfocusedTextColor = colorResource(id = R.color.black),
                focusedContainerColor = colorResource(id = R.color.white),
                unfocusedContainerColor = colorResource(id = R.color.white),
                disabledContainerColor = colorResource(id = R.color.white),
                errorContainerColor = colorResource(id = R.color.white),
                errorIndicatorColor = colorResource(id = R.color.red)
            )
        )
        error?.let {
            Text(
                text = it,
                color = colorResource(id = R.color.red),
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

    }
}

//Contraseña  2 ------------------------------------------------
@Composable
fun PasswordFieldReg2(password: String, error: String?, onTextFieldChanged: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) } //mostrar/ocultar contraseña
    Column(modifier = Modifier.padding(top = 1.dp, bottom=1.dp, end = 20.dp, start = 20.dp ))
    {
        TextField(
            value = password,
            onValueChange = { onTextFieldChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .border(2.dp, Color.Black),
            placeholder = { Text("Repite tu contraseña") },
            singleLine = true,
            isError = error != null,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image,
                        contentDescription = "Mostrar contraseña",
                        tint = colorResource(id = R.color.black))
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = colorResource(id = R.color.black),
                unfocusedTextColor = colorResource(id = R.color.black),
                focusedContainerColor = colorResource(id = R.color.white),
                unfocusedContainerColor = colorResource(id = R.color.white),
                disabledContainerColor = colorResource(id = R.color.white),
                errorContainerColor = colorResource(id = R.color.white),
                errorIndicatorColor = colorResource(id = R.color.red)
            )
        )
        error?.let {
            Text(
                text = it,
                color = colorResource(id = R.color.red),
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

    }
}
//fecha de nacimiento

@Composable
fun FechaNacimientoField(viewModel: RegisterViewModel) {
    val fecha by viewModel.date.collectAsState()
    val mostrarDialog by viewModel.showDatePicker.collectAsState()
    val dateError by viewModel.dateError.collectAsState()

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val anio = calendar.get(Calendar.YEAR)
    val mes = calendar.get(Calendar.MONTH)
    val dia = calendar.get(Calendar.DAY_OF_MONTH)

    if (mostrarDialog) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                viewModel.dateSeleccionada(year, month, dayOfMonth)
            },
            anio, mes, dia
        ).show()
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(start = 20.dp, end = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = fecha,
                onValueChange = {},
                label = { Text("Fecha de nacimiento", color = colorResource(id = R.color.black)) },
                modifier = Modifier
                    .weight(1f)
                    .height(70.dp)
                    .border(2.dp, Color.Black),
                readOnly = true,
                enabled = false,
                colors = TextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    focusedContainerColor = colorResource(id = R.color.white),
                    unfocusedContainerColor = colorResource(id = R.color.white),
                    disabledContainerColor = colorResource(id = R.color.white)
                )
            )

            IconButton(onClick = { viewModel.showMenuDate() }) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = "Seleccionar fecha"
                )
            }
        }

        if (dateError.isNotEmpty()) {
            Text(
                text = dateError,
                color = colorResource(id = R.color.red),
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

//boton de acceder/registro
@Composable
fun RegisterButtonReg(viewModel: RegisterViewModel) {
    Button(
        onClick = { viewModel.botonRegistro() },
        modifier = Modifier
            .height(48.dp).width(250.dp),
        colors = ButtonDefaults.buttonColors(
            colorResource(id= R.color.green),//boton habilitado
            colorResource(id= R.color.white),//boton desabilitado
            colorResource(id= R.color.green), //color contenido
            disabledContentColor = colorResource(id= R.color.green)

        ), enabled = true
    ) {
        Text(text = "Registrarse",
            fontSize = 20.sp,
            color = colorResource(id = R.color.black),
        )
    }
}


