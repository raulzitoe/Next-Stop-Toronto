package com.raulvieira.nextstoptoronto.screens.stopinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import kotlinx.coroutines.delay

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
                StopInfoCard(
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
    routeTag: String,
    stopTitle: String = "",
    onClick: (String) -> Unit,
    onClickFavorite: (Boolean) -> Unit,
    favoriteButtonChecked: Boolean
) {
    Card(
        modifier = Modifier
            .wrapContentHeight()
            .padding(10.dp), onClick = { }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .wrapContentSize()
                            .defaultMinSize(minWidth = 80.dp)
                            .padding(top = 10.dp, bottom = 10.dp, end = 10.dp),
                        color = Color.Red,
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Text(
                            text = routeTag,
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 5.dp),
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp
                        )
                    }
                    Text(predictionInfo.title)

                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Stop: $stopTitle")
                    FavoritesButton(
                        onChecked = { checkedValue -> onClickFavorite(checkedValue) },
                        isChecked = favoriteButtonChecked
                    )
                }

                predictionInfo.predictions.forEach { stopPrediction ->
                    Row() {
                        val counter = remember { mutableStateOf(0) }
                        LaunchedEffect(key1 = counter.value) {
                            delay(1000)
                            counter.value++
                        }
                        LaunchedEffect(key1 = stopPrediction.seconds) {
                            counter.value = 0
                        }
                        val predictionSeconds = stopPrediction.seconds.toInt() - counter.value
                        val minutes = predictionSeconds / 60
                        val seconds = predictionSeconds % 60
                        Text(text = "Vehicle: " + stopPrediction.vehicle + " - ")
                        Text(text = "In %02d:%02d".format(minutes, seconds))

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