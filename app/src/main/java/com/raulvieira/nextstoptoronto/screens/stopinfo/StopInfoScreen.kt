package com.raulvieira.nextstoptoronto.screens.stopinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulvieira.nextstoptoronto.components.InternetStatusBar
import com.raulvieira.nextstoptoronto.components.StopsPredictionLazyColumn
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import com.raulvieira.nextstoptoronto.models.SinglePredictionModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import isInternetOn
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopInfoScreen(
    viewModel: StopInfoViewModel = hiltViewModel(),
    routeTag: String,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val isInternetOn by isInternetOn(LocalContext.current, scope).collectAsStateWithLifecycle()
    var internetStatusBarVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isInternetOn) {
        internetStatusBarVisible = if (!isInternetOn) {
            true
        } else {
            viewModel.subscribeToStopStream()
            delay(2000)
            false
        }
    }

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
                    when (val state = uiState) {
                        is StopInfoScreenState.Success -> Text(state.data.predictions.first().stopTitle)
                        else -> Text("N/A")
                    }
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
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
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
                        is StopInfoScreenState.Loading -> StopInfoScreenLoading()
                        is StopInfoScreenState.Success -> {
                            val stopPrediction = state.data
                            StopInfoScreenSuccess(
                                stopPrediction = stopPrediction,
                                routeTag = routeTag,
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
                                })

                        }

                        is StopInfoScreenState.Error -> StopInfoScreenError()
                    }
                }

            }
        }
    )
}

@Composable
private fun StopInfoScreenSuccess(
    stopPrediction: StopPredictionModel,
    routeTag: String,
    onClickFavoriteItem: (Boolean, RoutePredictionsModel) -> Unit,
    favoriteButtonChecked: @Composable (RoutePredictionsModel) -> Boolean,
) {
    Column {
        StopsPredictionLazyColumn(
            predictions = stopPrediction.predictions.sortedByDescending {
                it.routeTag == routeTag
            },
            onClickFavoriteItem = { isChecked, favoriteItem ->
                onClickFavoriteItem(isChecked, favoriteItem)
            },
            favoriteButtonChecked = { prediction ->
                favoriteButtonChecked(prediction)
            },
            distanceToStop = { "" },
            hideEmptyRoute = false,
            isOnStopScreen = true
        )
    }
}

@Composable
private fun StopInfoScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun StopInfoScreenError() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ERROR")
    }
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
        distanceToStop = { "" }
    )
}