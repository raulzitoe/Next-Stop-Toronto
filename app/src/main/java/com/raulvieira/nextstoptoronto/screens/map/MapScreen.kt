package com.raulvieira.nextstoptoronto.screens.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel


@Composable
fun SecondScreen(
    viewModel: MapScreenViewModel = hiltViewModel()
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            MapView(
                onRequestStopInfo = { stopId -> viewModel.getStopPrediction(stopId) },
                stopState = viewModel.stopState,
                routes = viewModel.getStops()
            )
        }
    }
}