package com.raulvieira.nextstoptoronto.screens.stopinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulvieira.nextstoptoronto.components.StopPredictionCard
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun StopInfoScreen(
    viewModel: StopInfoViewModel = hiltViewModel(),
    routeTag: String,
    stopId: String,
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
                    RoutesLazyList(
                        routePredictions = uiState.predictions.filter {
                            it.routeTag == routeTag
                        },
                        checkFavoritedItem = { prediction ->
                            val isFavorited by viewModel.isRouteFavorited(
                                prediction.stopTag,
                                prediction.routeTag,
                                prediction.stopTitle
                            ).collectAsStateWithLifecycle(initialValue = false)
                            isFavorited
                        },
                        handleFavoriteCheck = { isChecked, favoriteModel ->
                            viewModel.handleFavoriteItem(isChecked, favoriteModel)
                        })
                    if (uiState.predictions.size > 1) {
                        Text("Other lines at this stop: ")
                        RoutesLazyList(
                            routePredictions = uiState.predictions.filter {
                                it.routeTag != routeTag
                            },
                            checkFavoritedItem = { prediction ->
                                val isFavorited by viewModel.isRouteFavorited(
                                    prediction.stopTag,
                                    prediction.routeTag,
                                    prediction.stopTitle
                                ).collectAsStateWithLifecycle(initialValue = false)
                                isFavorited
                            },
                            handleFavoriteCheck = { isChecked, favoriteModel ->
                                viewModel.handleFavoriteItem(isChecked, favoriteModel)
                            })
                    }
                }
            }
        }
    )
}

@Composable
fun RoutesLazyList(
    routePredictions: List<RoutePredictionsModel>,
    checkFavoritedItem: @Composable (RoutePredictionsModel) -> Boolean,
    handleFavoriteCheck: (Boolean, FavoritesModel) -> Unit
) {
    LazyColumn() {
        items(routePredictions) { prediction ->

            prediction.directions?.forEach { direction ->
                StopPredictionCard(
                    predictionInfo = direction,
                    routeTag = prediction.routeTag,
                    onClick = { },
                    onClickFavorite = { isChecked ->
                        handleFavoriteCheck(
                            isChecked,
                            FavoritesModel(
                                id = 0,
                                routeTag = prediction.routeTag,
                                stopTag = prediction.stopTag,
                                stopTitle = prediction.stopTitle
                            )
                        )
                    },
                    favoriteButtonChecked = checkFavoritedItem(prediction),
                    distanceToStop = { "" }
                )

            }
        }
    }
}