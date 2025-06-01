@file:Suppress("DEPRECATION")

package com.example.tfg_1.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80 ,

    background = Color.Black,
    surface = Color.Black,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    background = Color.LightGray,
    surface = Color.LightGray,

    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun Tfg_1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(), //modo oscuro
    //color dinamico (Android 12+)
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit//contenido donde se aplican clores
) {
    val colorScheme = when {
        //usar colores dinamicos si estan disponibles
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        //si no hay colores dinámicos usaar los predefinidos
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current //actual vista
    if (!view.isInEditMode) { //cuando no esta en vista previa
        SideEffect { //
            val window = (view.context as Activity).window
            //color de barra de estado
            window.statusBarColor = colorScheme.primary.toArgb()
            // color del texto de la barra de estado
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    //se aplica a todo el contenido
    MaterialTheme(
        colorScheme = colorScheme, //esquema de colores
        typography = Typography, //tipografía
        content = content
    )
}
