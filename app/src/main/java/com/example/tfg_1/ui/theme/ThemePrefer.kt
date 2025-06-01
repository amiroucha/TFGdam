package com.example.tfg_1.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "theme_prefs" //guardar preferencias
//acceder a DataStore
private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)
// Clave de acceso al booleano que indica si el modo oscuro esta activado
private val DARK_KEY = booleanPreferencesKey("dark_mode")

class ThemePrefer(private val context: Context) {

    // Lee valor desde DataStore  si no existe --> devuelve false (modo claro por defecto)
    val isDark: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_KEY] ?: false
    }

    //Guardar  elección del usuario (modo oscuro activado o desactivado)
    suspend fun saveDark(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_KEY] = enabled
        }
    }
}