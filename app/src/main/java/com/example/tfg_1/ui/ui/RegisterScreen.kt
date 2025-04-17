package com.example.tfg_1.ui.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.material.icons.filled.CalendarToday


@Preview(showBackground = true)
@Composable
fun registerScreenPreview() {
    val navController = rememberNavController()
    val viewModel = RegisterViewModel(navController)
    RegisterScreen(viewModel = viewModel, navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(viewModel: RegisterViewModel, navcontroller : NavController) {
    /*
    *    Box(
        Modifier
            .fillMaxSize()
            //.padding(5.dp)
            .background( color = colorResource(id = R.color.greyBackground))
    ) {
        RegisterBody(Modifier.align(Alignment.Center), viewModel, navcontroller)
    }
    * */
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = rememberTopAppBarState())
    Scaffold(
        topBar = { TopBar(navcontroller) }
    ) {
        paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                //.padding(5.dp)
                .background( color = colorResource(id = R.color.greyBackground))
                //.padding(paddingValues)
        ) {
            RegisterBody(Modifier.align(Alignment.Center).padding(paddingValues), viewModel ,navcontroller )
        }

    }
}


@Composable
fun TopBar(navcontroller: NavController)
{
    Icon(imageVector = Icons.Default.ArrowBack,
        contentDescription = "Back",
        modifier = Modifier
            .clickable{
                navcontroller.popBackStack()
            }
            .padding(top = 40.dp, start = 10.dp)
            .size(34.dp)
    )

}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterBody (modifier: Modifier, viewModel: RegisterViewModel, navcontroller : NavController) {
    val email by viewModel.email.collectAsState()
    val passwordR by viewModel.password.collectAsState()
    val password2 by viewModel.password2.collectAsState()
    val isRegisterEnabled by viewModel.registerEnable.collectAsState()
    val isLoading by viewModel.isLoadingR.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError1 by viewModel.passwordError.collectAsState()
    val passwordError2 by viewModel.passwordError.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }else{
        Column(modifier = modifier.fillMaxSize().padding(top = 16.dp)) {
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
            EmailFieldReg(email, error = emailError)
            { viewModel.onLoginChanges(it, passwordR, password2) }
            Spacer(modifier = Modifier.padding(4.dp))
            PasswordFieldReg(passwordR, error = passwordError1) { viewModel.onLoginChanges(email, it, password2) }
            Spacer(modifier = Modifier.padding(4.dp))
            PasswordFieldReg2(password2, error = passwordError2) { viewModel.onLoginChanges(email,passwordR, it) }
            //Spacer(modifier = Modifier.padding(15.dp))
            DatePicker()
            Spacer(modifier = Modifier.padding(15.dp))
            Column(modifier = Modifier.align(Alignment.End).padding(end = 20.dp))
            {
                RegisterButtonReg(isRegisterEnabled) {

                }
            }

            Spacer(modifier = Modifier.padding(20.dp))
            Column(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 25.dp))
            {
                //backButton(isRegisterEnabled) {

                //}
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
        modifier = modifier
            .padding(top = 0.dp, bottom= 5.dp, end = 5.dp, start = 5.dp),
        fontSize = 50.sp,
        maxLines = 1, // Solo permite 1 línea
        fontWeight = FontWeight.Bold
    )
}
//email ----------------------------------------------------------------
@Composable
fun EmailFieldReg(email: String,error: String?, onTextFieldChanged: (String) -> Unit) {
    Row(modifier = Modifier.padding(20.dp,10.dp)) {
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
                focusedContainerColor = colorResource(id = R.color.black),
                unfocusedTextColor = colorResource(id = R.color.black),
                unfocusedContainerColor = colorResource(id = R.color.white),
                errorIndicatorColor = colorResource(id = R.color.red)
            )
        )
        error?.let { //si el error!=null -> hay error , entonces:
            Text(
                text = it,//it es el valor del error (no null)
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
//Contraseña 1------------------------------------------------
@Composable
fun PasswordFieldReg(password: String, error: String?, onTextFieldChanged: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) } //mostrar/ocultar contraseña
    Row(modifier = Modifier.padding(20.dp,10.dp))
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
                    Icon(imageVector = image, contentDescription = "Mostrar contraseña")
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = colorResource(id = R.color.white),
                focusedContainerColor = colorResource(id = R.color.black),
                unfocusedTextColor = colorResource(id = R.color.black),
                unfocusedContainerColor = colorResource(id = R.color.white),
                errorIndicatorColor = Color.Red
            )
        )
        error?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

    }
}

//Contraseña  2 ------------------------------------------------
@Composable
fun PasswordFieldReg2(password: String, error: String?, onTextFieldChanged: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) } //mostrar/ocultar contraseña
    Row(modifier = Modifier.padding(20.dp,10.dp))
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
                    Icon(imageVector = image, contentDescription = "Mostrar contraseña")
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = colorResource(id = R.color.white),
                focusedContainerColor = colorResource(id = R.color.black),
                unfocusedTextColor = colorResource(id = R.color.black),
                unfocusedContainerColor = colorResource(id = R.color.white),
                errorIndicatorColor = Color.Red
            )
        )
        error?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

    }
}

@Composable
fun DatePicker() {
    // Manejamos el estado de la fecha seleccionada
    val selectedDate = remember { mutableStateOf("") }

    // Estado para abrir o cerrar el DatePicker
    val openDatePicker = remember { mutableStateOf(false) }

    // Crear un calendario (similar a Calendar.getInstance())
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

    // Mostrar el DatePicker cuando el usuario hace clic
    if (openDatePicker.value) {
        DatePickerDialog(
            onDateSelected = { year, month, day ->
                // Aquí se formatea la fecha seleccionada
                val formattedDate = String.format(
                    "%02d/%02d/%04d", day, month + 1, year
                )
                selectedDate.value = formattedDate
                openDatePicker.value = false // Cerrar el DatePicker después de seleccionar
            },
            initialYear = currentYear,
            initialMonth = currentMonth,
            initialDay = currentDay
        )
    }

    // Interfaz con un TextField para mostrar la fecha seleccionada
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Fecha de nacimiento:")

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = selectedDate.value,
                onValueChange = {},
                enabled = false, // El campo solo se puede leer
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                label = { Text("Selecciona la fecha") }
            )

            IconButton(onClick = { openDatePicker.value = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
            }
        }
    }
}

// menu DatePicker
@Composable
fun DatePickerDialog(
    onDateSelected: (Int, Int, Int) -> Unit,
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int
) {
    val context = LocalContext.current
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(year, month, dayOfMonth)
            },
            initialYear,
            initialMonth,
            initialDay
        )
    }

    LaunchedEffect(Unit) {
        datePickerDialog.show()
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


