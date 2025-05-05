package com.example.tfg_1.ui.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    currentHomeId: String,
    onCreateHome: (String, String) -> Unit,
    onJoinHome: (String) -> Unit,
    membersFlow: (String) -> Unit
) {

}
