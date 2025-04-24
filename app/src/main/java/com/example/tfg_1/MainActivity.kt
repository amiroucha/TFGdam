package com.example.tfg_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.tfg_1.navigation.NavigationWrapper
import com.example.tfg_1.ui.theme.Tfg_1Theme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Tfg_1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavigationWrapper()
                }
            }

        }
    }
}

