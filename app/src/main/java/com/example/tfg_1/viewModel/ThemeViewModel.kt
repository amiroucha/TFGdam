package com.example.tfg_1.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_1.ui.theme.ThemePrefer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = ThemePrefer(app)

    private val _isDark = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDark

    init {
        // lee el valor persistido cada vez que cambie.
        viewModelScope.launch {
            prefs.isDark.collect { saved ->
                _isDark.value = saved
            }
        }
    }

    //se cambia desde settings
    fun toggleTheme(enabled: Boolean) {
        viewModelScope.launch {
            prefs.saveDark(enabled)      // guarda en DataStore
            _isDark.value = enabled      // actualiza valor
        }
    }
}