package com.example.tfg_1.ui.ui

import androidx.compose.foundation.*
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
    LoginScreen(viewModel = viewModel, navController = navController)
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    navController: NavController
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.greyBackground))
    ) {
        LoginBody(Modifier.align(Alignment.Center), viewModel, navController)
    }
}

@Composable
fun LoginBody(modifier: Modifier, viewModel: LoginViewModel, navController: NavController) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val passwordResetMessage by viewModel.passwordResetMessage.collectAsState()


    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.size(48.dp).align(Alignment.Center))
        }
    } else {
        val scrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)//se pueda desplazar verticalmente
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.app),
                        modifier = Modifier.padding(start = 8.dp, end = 15.dp),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    LogoHeader(Modifier)
                }
            }

            TituloLogin(Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.padding(5.dp))

            EmailField(email = email, error = emailError) {
                viewModel.onLoginChanges(it, password)
            }

            Spacer(modifier = Modifier.padding(4.dp))

            PasswordField(password = password, error = passwordError) {
                viewModel.onLoginChanges(email, it)
            }

            ForgotPassword(Modifier.align(Alignment.End), viewModel)


            if (passwordResetMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearPasswordResetMessage() },
                    title = { Text(stringResource(R.string.RecuperaContrasenia)) },
                    text = { Text(passwordResetMessage!!) },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.clearPasswordResetMessage() }
                        ) {
                            Text(stringResource(R.string.aceptar))
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.padding(16.dp))

            //alert errores de firebase-------------------------
            var showDialog by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf("") }
            LaunchedEffect(authState) {
                if (authState is LoginViewModel.AuthState.Error) {
                    errorMessage = (authState as LoginViewModel.AuthState.Error).error
                    showDialog = true
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(text = "Error de inicio de sesión") },
                    text = { Text(text = errorMessage) },
                    confirmButton = {
                        Button(
                            onClick = { showDialog = false }
                        ) {
                            Text("Aceptar")
                        }
                    }
                )
            }
            //--------------------------------------------------

            Column(modifier = Modifier.align(Alignment.End).padding(end = 20.dp)) {

                LoginButton(viewModel, navController)

                Spacer(modifier = Modifier.padding(16.dp))
                GoogleButton(viewModel, navController)
            }

            Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Spacer(modifier = Modifier.padding(16.dp))
                Text(
                    text = "¿ No tienes cuenta ?",
                    fontSize = 20.sp,
                    color = colorResource(id = R.color.black),
                    modifier = Modifier.padding(10.dp)
                        .align(Alignment.CenterHorizontally)
                )
                RegisterButton(navController)
            }
        }
    }
}

// Imagen Logo
@Composable
fun LogoHeader(modifier: Modifier) {
    Image(
        painter = painterResource(id = R.drawable.logotfg),
        contentDescription = "Hogar",
        modifier = modifier
            .padding(10.dp)
            .size(120.dp)
            .clip(CircleShape)
    )
}

// Título
@Composable
fun TituloLogin(modifier: Modifier) {
    Text(
        text = "Iniciar Sesión",
        modifier = modifier.padding(10.dp),
        fontSize = 40.sp,
        maxLines = 1,
        fontWeight = FontWeight.Bold
    )
}

// EmailField con error
@Composable
fun EmailField(email: String, error: String?, onTextFieldChanged: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
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
                color = colorResource(id = R.color.red),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// PasswordField con error
@Composable
fun PasswordField(password: String, error: String?, onTextFieldChanged: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
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
                unfocusedTextColor = colorResource(id = R.color.black),
                focusedContainerColor = colorResource(id = R.color.black),
                unfocusedContainerColor = colorResource(id = R.color.white),
                errorIndicatorColor = colorResource(id = R.color.red)
            )
        )
        error?.let {
            Text(
                text = it,
                color = colorResource(id = R.color.red),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun ForgotPassword(modifier: Modifier, viewModel: LoginViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }

    Text(
        text = "¿Olvidaste la contraseña?",
        modifier = modifier
            .clickable { showDialog = true }
            .padding(start = 20.dp, end = 20.dp, top = 5.dp, bottom = 16.dp),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = colorResource(id = R.color.black),
        textDecoration = TextDecoration.Underline
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Recuperar contraseña") },
            text = {
                Column {
                    Text("Introduce tu correo para recuperar tu contraseña:")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (emailInput.isBlank()) {
                        // mensaje de error si lo quieres mostrar
                        viewModel.setPasswordResetError("Por favor, introduce un email válido")
                    } else {
                        viewModel.sendResetPassword(emailInput)
                        showDialog = false
                    }
                }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


@Composable
fun LoginButton(viewModel: LoginViewModel, navController: NavController) {
    Button(
        onClick = {
            viewModel.login(navController)
        },
        modifier = Modifier
            .height(48.dp)
            .width(250.dp),
        colors = ButtonDefaults.buttonColors(
            colorResource(id = R.color.brown)
        )
    ) {
        Text(
            text = "Iniciar sesión",
            fontSize = 20.sp,
            color = colorResource(id = R.color.black)
        )
    }
}

@Composable
fun GoogleButton(viewModel: LoginViewModel, navController: NavController) {
    val context = LocalContext.current
    Button(
        onClick = {
            viewModel.loginGoogle(context, navController)
        },
        modifier = Modifier
            .height(48.dp)
            .width(250.dp),
        colors = ButtonDefaults.buttonColors(
            Color(0xFFFF4303),
            Color(0xFFF78058),
            colorResource(id = R.color.blue),
            disabledContentColor = Color.White
        )
    ) {
        Text(
            text = "Iniciar con Google",
            fontSize = 20.sp,
            color = colorResource(id = R.color.black)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_google),
            contentDescription = "Google",
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
    }
}

@Composable
fun RegisterButton(navController: NavController) {
    Button(
        onClick = { navController.navigate(Screens.Register.route) },
        modifier = Modifier
            .height(48.dp)
            .width(250.dp),
        colors = ButtonDefaults.buttonColors(
            colorResource(id = R.color.brownRegister)
        )
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Registrate aquí",
            fontSize = 20.sp,
            color = colorResource(id = R.color.black)
        )
    }
}
