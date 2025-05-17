package com.example.tfg_1.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "theme_prefs"
private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)
private val DARK_KEY = booleanPreferencesKey("dark_mode")

class ThemePrefer(private val context: Context) {

    // guardar modo oscuro
    val isDark: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_KEY] ?: false
    }

    //Guardar  elección del usu
    suspend fun saveDark(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_KEY] = enabled
        }
    }
}