package com.raulvieira.nextstoptoronto.screens.stopinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun StopInfoScreen(
    viewModel: StopInfoViewModel = hiltViewModel(),
    routeTag: String,
    stopId: String,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle


    LaunchedEffect(key1 = Unit) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            launch {
                viewModel.getStopPrediction(stopId = stopId)
            }
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
                            var isFavorited by remember { mutableStateOf(false) }
                            LaunchedEffect(key1 = prediction.stopTag, key2 = prediction.routeTag) {
                                lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                                    launch {
                                        viewModel.isRouteFavorited(
                                            prediction.stopTag,
                                            prediction.routeTag,
                                            prediction.stopTitle
                                        ).collect {
                                            isFavorited = it
                                        }
                                    }
                                }
                            }
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
                                var isFavorited by remember { mutableStateOf(false) }
                                LaunchedEffect(
                                    key1 = prediction.stopTag,
                                    key2 = prediction.routeTag
                                ) {
                                    lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                                        launch {
                                            viewModel.isRouteFavorited(
                                                prediction.stopTag,
                                                prediction.routeTag,
                                                prediction.stopTitle
                                            ).collect {
                                                isFavorited = it
                                            }
                                        }
                                    }
                                }
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

            prediction.directions.forEach { direction ->
                StopInfoCard(
                    predictionInfo = direction,
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
                    favoriteButtonChecked = checkFavoritedItem(prediction)
                )

            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopInfoCard(
    predictionInfo: PredictionModel,
    onClick: (String) -> Unit,
    onClickFavorite: (Boolean) -> Unit,
    favoriteButtonChecked: Boolean
) {
    Card(
        modifier = Modifier
            .wrapContentHeight()
            .padding(10.dp), onClick = { }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Text(predictionInfo.title)
                FavoritesButton(
                    onChecked = { checkedValue -> onClickFavorite(checkedValue) },
                    isChecked = favoriteButtonChecked
                )
                predictionInfo.predictions.forEach {
                    Row() {
                        Text(text = "Vehicle: " + it.vehicle + " - ")
                        Text(text = "In " + it.minutes + " minutes")
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesButton(isChecked: Boolean, onChecked: (Boolean) -> Unit) {
    IconToggleButton(checked = isChecked, onCheckedChange = {
        onChecked(it)
    }) {
        if (isChecked) {
            Icon(Icons.Filled.Favorite, contentDescription = "Localized description")
        } else {
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Localized description")
        }
    }
}