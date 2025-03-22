package com.example.tfg_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.tfg_1.ui.ui.Login
import com.example.tfg_1.viewModel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            Login(LoginViewModel())

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
