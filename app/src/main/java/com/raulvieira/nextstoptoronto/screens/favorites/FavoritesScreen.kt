package com.raulvieira.nextstoptoronto.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.components.StopsPredictionLazyColumn
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import com.raulvieira.nextstoptoronto.models.SinglePredictionModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavoriteEmpty by viewModel.isFavoriteEmpty.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.favorites))
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if(isFavoriteEmpty){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("There are no routes on your favorites")
                }
            }
            else if (uiState.predictions.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else {
                StopsPredictionLazyColumn(
                    predictions = uiState.predictions,
                    onClickFavoriteItem = { isChecked, favoriteItem ->
                        viewModel.handleFavoriteItem(
                            isChecked,
                            FavoritesModel(
                                id = 0,
                                routeTag = favoriteItem.routeTag,
                                stopTag = favoriteItem.stopTag,
                                stopTitle = favoriteItem.stopTitle
                            )
                        )
                    },
                    favoriteButtonChecked = { routeToCheck ->
                        viewModel.isRouteFavorited(
                            routeToCheck.stopTag,
                            routeToCheck.routeTag,
                            routeToCheck.stopTitle
                        ).collectAsStateWithLifecycle(initialValue = false).value
                    },
                    distanceToStop = { "" },
                    hideEmptyRoute = false
                )
            }

        }
    }
}

@Preview
@Composable
fun StopsLazyColumnPreview() {
    StopsPredictionLazyColumn(
        predictions = listOf(
            RoutePredictionsModel(
                routeTag = "41",
                stopTag = "1234",
                routeTitle = "41-Keele Towards somewhere",
                stopTitle = "Keele St at that St",
                directionTitleWhenNoPredictions = "41 - Keele some short turn",
                directions = listOf(
                    PredictionModel(
                        title = "41-Keele Towards somewhere",
                        predictions = listOf(
                            SinglePredictionModel(
                                "41",
                                vehicle = "1234",
                                minutes = "1",
                                seconds = "1"
                            ),
                            SinglePredictionModel(
                                "41",
                                vehicle = "1234",
                                minutes = "1",
                                seconds = "1"
                            )
                        )
                    )
                )
            ),
            RoutePredictionsModel(
                routeTag = "41",
                stopTag = "1234",
                routeTitle = "41-Keele Towards somewhere",
                stopTitle = "Keele St at that St",
                directionTitleWhenNoPredictions = "41 - Keele some short turn",
                directions = listOf(
                    PredictionModel(
                        title = "41-Keele Towards somewhere",
                        predictions = listOf(
                            SinglePredictionModel(
                                "41",
                                vehicle = "1234",
                                minutes = "1",
                                seconds = "1"
                            ),
                            SinglePredictionModel(
                                "41",
                                vehicle = "1234",
                                minutes = "1",
                                seconds = "1"
                            )
                        )
                    )
                )
            )
        ),
        onClickFavoriteItem = { _, _ -> },
        favoriteButtonChecked = { true },
        distanceToStop = {"0.2 Km"}
    )
}