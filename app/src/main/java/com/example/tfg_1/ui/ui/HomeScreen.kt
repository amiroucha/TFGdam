package com.example.tfg_1.ui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tfg_1.R
import com.example.tfg_1.viewModel.HomeViewModel

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    val viewModel = HomeViewModel()
    HomeScreen(viewModel = viewModel, navController)
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController
) {
    Box(
        Modifier
            .fillMaxSize()
            //.padding(5.dp)
            .background( color = colorResource(id = R.color.greyBackground))
    ) {
        HomeBody(Modifier.align(Alignment.Center), viewModel, navController)
    }
}





@Composable
fun HomeBody(modifier: Modifier, viewModel: HomeViewModel, navController: NavController) {

    Row (modifier.fillMaxSize()) {
        Text(
            text="Hola esto es home"
            ,modifier = Modifier
                .padding(top = 55.dp)
                .align(Alignment.CenterVertically)
            , fontSize = 30.sp
        )
    }
}