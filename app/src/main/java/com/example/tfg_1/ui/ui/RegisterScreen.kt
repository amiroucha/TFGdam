package com.example.tfg_1.ui.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.tfg_1.R
import com.example.tfg_1.viewModel.RegisterViewModel
import java.util.Calendar
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.lazy.LazyColumn


@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    val navController = rememberNavController()
    val viewModel = RegisterViewModel(navController)
    RegisterScreen(viewModel = viewModel/*, navController*/)
}

@Composable
fun RegisterScreen(viewModel: RegisterViewModel/*, navcontroller : NavController*/) {
    Box(
        Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.greyBackground))
    ) {
        RegisterBody(
            Modifier
                .align(Alignment.Center)
                .padding(10.dp),
            viewModel
        )
    }

}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterBody (modifier: Modifier, viewModel: RegisterViewModel) {
    val email by viewModel.email.collectAsState()
    val name by viewModel.name.collectAsState()
    val passwordR by viewModel.password.collectAsState()
    val password2 by viewModel.password2.collectAsState()
    val isLoading by viewModel.isLoadingR.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val nameError by viewModel.nameError.collectAsState()
    val passwordError1 by viewModel.passwordError.collectAsState()
    val passwordError2 by viewModel.passwordError2.collectAsState()
    val passwordSameError by viewModel.passwordsame.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }else{
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, bottom = 16.dp, end = 16.dp, start = 16.dp),
                    contentAlignment = Alignment.Center
                )
                {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically // Alinea verticalmente al centro
                    ) {
                        Text(
                            text = stringResource(R.string.app),
                            modifier = Modifier.align(Alignment.CenterVertically), // Espaciado a la izquierda
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )

                        // LogoHeaderReg(Modifier) // Ajusta el tamaño del logo según sea necesario
                    }
                }
            }
            item {
                TituloRegister(Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
            }
            item {
                NameFieldReg(name = name, error = nameError){ viewModel.onLoginChanges(email, passwordR, password2, it) }
            }
            item {
                Spacer(modifier = Modifier.padding(5.dp))
            }
            item {
                EmailFieldReg(email, error = emailError) { viewModel.onLoginChanges(it, passwordR, password2, name) }
            }
            item {
                Spacer(modifier = Modifier.padding(2.dp))
            }
                item {
                PasswordFieldReg(passwordR, error = passwordError1) {
                    viewModel.onLoginChanges(
                        email,
                        it,
                        password2,
                        name
                    )
                }
            }
                item {
                Spacer(modifier = Modifier.padding(4.dp))
            }
            item {
                PasswordFieldReg2(password2, error = passwordError2)
                { viewModel.onLoginChanges(email, passwordR, it, name) }
            }
            item {
                // error de contraseñas diferentes
                if (passwordSameError.isNotEmpty()) {
                    Text(
                        text = passwordSameError,
                        color = colorResource(id = R.color.red),
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 20.dp)
                    )
                }
            }
            item {
                FechaNacimientoField(viewModel)
            }
            item {
                Spacer(modifier = Modifier.padding(15.dp))
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.End)
                        .padding(end = 20.dp)
                )
                {
                    RegisterButtonReg(viewModel)
                }
            }
            item {
                Spacer(modifier = Modifier.padding(20.dp))
            }
        }

    }
}

//Titulo---------------------------------------------
@Composable
fun TituloRegister(modifier:Modifier){
    Text( text = stringResource(R.string.registro) ,
        modifier = modifier.padding(top = 0.dp, bottom= 5.dp, end = 5.dp, start = 5.dp),
        fontSize = 40.sp,
        maxLines = 1, // Solo permite 1 línea
        fontWeight = FontWeight.Bold
    )
}
//nombre ----------------------------------------------------------------
@Composable
fun NameFieldReg(name: String, error: String?, onTextFieldChanged: (String) -> Unit) {
    Column (modifier = Modifier.padding(top = 5.dp, bottom=1.dp, end = 20.dp, start = 20.dp )) {
        TextField(
            value = name,
            onValueChange = { onTextFieldChanged(it) },//actualiza rl valor
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .border(2.dp, Color.Black),
            placeholder = { Text(text = stringResource(R.string.nombre)) },
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
            placeholder = { Text(text = stringResource(R.string.email)) },
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
            placeholder = { Text(stringResource(R.string.contraseña)) },
            singleLine = true,
            isError = error != null,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image,
                        contentDescription = stringResource(R.string.mostrarContraseña),
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
            placeholder = { Text(stringResource(R.string.repetirContraseña)) },
            singleLine = true,
            isError = error != null,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image,
                        contentDescription = stringResource(R.string.mostrarContraseña),
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
    val fecha by viewModel.birthdate.collectAsState()
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
                label = { Text(stringResource(R.string.fechanacimiento), color = colorResource(id = R.color.black)) },
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
                    contentDescription = stringResource(R.string.seleccionaFecha)
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
    val context = LocalContext.current
    Button(
        onClick = { viewModel.botonRegistro(context) },
        modifier = Modifier
            .height(48.dp)
            .width(250.dp),
        colors = ButtonDefaults.buttonColors(
            colorResource(id= R.color.green),//boton habilitado
            colorResource(id= R.color.white),//boton desabilitado
            colorResource(id= R.color.green), //color contenido
            disabledContentColor = colorResource(id= R.color.green)

        ), enabled = true
    ) {
        Text(text = stringResource(R.string.registrarseBtn),
            fontSize = 20.sp,
            color = colorResource(id = R.color.black),
        )
    }
}


