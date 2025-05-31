package com.example.tfg_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg_1.navigation.NavigationWrapper
import com.example.tfg_1.ui.theme.Tfg_1Theme
import com.example.tfg_1.viewModel.ThemeViewModel
import androidx.compose.runtime.getValue
import com.google.android.material.color.DynamicColors


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Aplica colores dinámicos basados en contenido
        DynamicColors.applyToActivityIfAvailable(this)

        // extender app a pantalla completa
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDark by themeViewModel.isDarkTheme.collectAsState()

            Tfg_1Theme(darkTheme = isDark, dynamicColor = true) {
                Surface(Modifier.fillMaxSize()) {
                    NavigationWrapper(themeViewModel = themeViewModel)
                }
            }
        }
    }
}


