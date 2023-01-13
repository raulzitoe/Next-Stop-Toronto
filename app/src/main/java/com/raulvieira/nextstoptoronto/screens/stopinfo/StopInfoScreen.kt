package com.raulvieira.nextstoptoronto.screens.stopinfo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulvieira.nextstoptoronto.components.StopsPredictionLazyColumn
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import com.raulvieira.nextstoptoronto.models.SinglePredictionModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopInfoScreen(
    viewModel: StopInfoViewModel = hiltViewModel(),
    routeTag: String,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                    Text(
                        if (uiState.predictions.isNotEmpty()) {
                            uiState.predictions.first().stopTitle
                        } else {
                            "N/A"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigateUp() }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Localized description")
                    }
                }
            )
        },
        content = { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                Column {
                    StopsPredictionLazyColumn(
                        predictions = uiState.predictions.filter {
                            it.routeTag == routeTag
                        },
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
                        favoriteButtonChecked = { prediction ->
                            val isFavorited by viewModel.isRouteFavorited(
                                prediction.stopTag,
                                prediction.routeTag,
                                prediction.stopTitle
                            ).collectAsStateWithLifecycle(initialValue = false)
                            isFavorited
                        },
                        distanceToStop = { "" },
                        hideEmptyRoute = false
                    )
                    if (uiState.predictions.size > 1) {
                        Text("Other lines at this stop: ")
                        StopsPredictionLazyColumn(
                            predictions = uiState.predictions.filter {
                                it.routeTag != routeTag
                            },
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
                            favoriteButtonChecked = { prediction ->
                                val isFavorited by viewModel.isRouteFavorited(
                                    prediction.stopTag,
                                    prediction.routeTag,
                                    prediction.stopTitle
                                ).collectAsStateWithLifecycle(initialValue = false)
                                isFavorited
                            },
                            distanceToStop = { "" },
                            hideEmptyRoute = false
                            )
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun StopInfoScreenLazyColumnPreview() {
    StopsPredictionLazyColumn(
        predictions = listOf(
            RoutePredictionsModel(
                routeTag = "41",
                stopTag = "1234",
                routeTitle = "41-Keele Towards somewhere",
                stopTitle = "Keele St at that St",
                directionTitleWhenNoPredictions = "41 - Some short turn",
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
                directionTitleWhenNoPredictions = "41 - Some short turn",
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
        distanceToStop = {""}
    )
}