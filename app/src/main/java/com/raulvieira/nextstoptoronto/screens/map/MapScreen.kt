package com.raulvieira.nextstoptoronto.screens.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.raulvieira.nextstoptoronto.MapView
import com.raulvieira.nextstoptoronto.navigation.Screen


@Composable
fun SecondScreen(
    viewModel: MapScreenViewModel = hiltViewModel(),
    onNavigate: (Screen) -> Unit
) {


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Text(text = "Second Screen", modifier = Modifier.clickable { onNavigate(Screen.Home) })
            MapView(
                onRequestStopInfo = { stopId -> viewModel.getStopPrediction(stopId) },
                stopState = viewModel.stopState,
            routes = viewModel.getStops())
        }

    }
}