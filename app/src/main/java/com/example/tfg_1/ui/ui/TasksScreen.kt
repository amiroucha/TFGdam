package com.example.tfg_1.ui.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tfg_1.R
import com.example.tfg_1.viewModel.TasksViewModel

@Preview(showBackground = true)
@Composable
fun TasksScreenPreview() {
    val navController = rememberNavController()
    val viewModel = TasksViewModel()
    TasksScreen(viewModel = viewModel, navController)
}

@Composable
fun TasksScreen(viewModel: TasksViewModel, navcontroller : NavController) {
    Scaffold(
        topBar = { TopBar(navcontroller) }
    ) {
            paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .background( color = colorResource(id = R.color.greyBackground))
        ) {
            TasksBody(Modifier.align(Alignment.Center).padding(paddingValues), viewModel ,navcontroller )
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TasksBody (modifier: Modifier, viewModel: TasksViewModel, navcontroller : NavController) {





}