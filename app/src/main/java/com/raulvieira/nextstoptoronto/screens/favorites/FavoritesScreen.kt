package com.raulvieira.nextstoptoronto.screens.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.components.InternetStatusBar
import com.raulvieira.nextstoptoronto.components.StopsPredictionLazyColumn
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import com.raulvieira.nextstoptoronto.models.SinglePredictionModel
import isInternetOn
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onClickRoute: (routeTag: String, stopTag: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val isInternetOn by isInternetOn(LocalContext.current, scope).collectAsStateWithLifecycle()
    var internetStatusBarVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isInternetOn) {
        internetStatusBarVisible = if (!isInternetOn) {
            true
        } else {
            delay(2000)
            false
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
            Column {
                AnimatedVisibility(
                    visible = internetStatusBarVisible,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    InternetStatusBar(isConnected = isInternetOn)
                }
                when (val state = uiState) {
                    is FavoritesScreenState.Loading -> FavoritesScreenLoading()

                    is FavoritesScreenState.Success -> {
                        val favoritesPrediction = state.data
                        FavoritesScreenSuccess(
                            predictions = favoritesPrediction.predictions,
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
                            onClickRoute = onClickRoute
                        )

                    }

                    is FavoritesScreenState.Error -> FavoritesScreenError()
                }
            }
        }
    }
}

@Composable
private fun FavoritesScreenSuccess(
    predictions: List<RoutePredictionsModel>,
    onClickFavoriteItem: (Boolean, RoutePredictionsModel) -> Unit,
    favoriteButtonChecked: @Composable (RoutePredictionsModel) -> Boolean,
    onClickRoute: (routeTag: String, stopTag: String) -> Unit
) {
    if (predictions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("There are no routes on your favorites")
        }
    } else {
        StopsPredictionLazyColumn(
            predictions = predictions,
            onClickFavoriteItem = { isChecked, favoriteItem ->
                onClickFavoriteItem(isChecked, favoriteItem)
            },
            favoriteButtonChecked = { routeToCheck ->
                favoriteButtonChecked(routeToCheck)
            },
            distanceToStop = { "" },
            hideEmptyRoute = false,
            onClickRoute = onClickRoute
        )
    }
}

@Composable
private fun FavoritesScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FavoritesScreenError() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ERROR")
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
        distanceToStop = { "0.2 Km" },
        onClickRoute = {_ , _ -> }
    )
}