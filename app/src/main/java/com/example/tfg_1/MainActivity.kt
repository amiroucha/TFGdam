package com.example.tfg_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.tfg_1.navigation.NavigationWrapper
import com.example.tfg_1.ui.theme.Tfg_1Theme
import com.example.tfg_1.ui.ui.LoginScreen
import com.example.tfg_1.viewModel.AuthViewModel
import com.example.tfg_1.viewModel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Tfg_1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    //LoginScreen(LoginViewModel())
                    NavigationWrapper()
                }
            }

        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewMain() {
    val navController = rememberNavController()
    val viewModel = AuthViewModel()
    LoginScreen(viewModel = viewModel, navController)
}

