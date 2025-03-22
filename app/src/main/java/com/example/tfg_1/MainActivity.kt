package com.example.tfg_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.example.tfg_1.ui.theme.Tfg_1Theme
import com.example.tfg_1.ui.ui.LoginScreen
import com.example.tfg_1.viewModel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Tfg_1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(id = R.color.greyBackground)
                ) {
                    LoginScreen(LoginViewModel())
                }
            }

        }
    }
}


/*
* Image(
            painter = painterResource(id= R.drawable.logotfg),
            contentDescription = "Hogar",
            modifier = Modifier
                .padding(5.dp)
                .size(79.dp)
                .clip(CircleShape)
                .border(7.dp, color = Color.Black)
            , contentScale = ContentScale.None,
        )
* */
