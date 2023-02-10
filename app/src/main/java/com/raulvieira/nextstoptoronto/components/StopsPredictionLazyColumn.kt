package com.raulvieira.nextstoptoronto.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import com.raulvieira.nextstoptoronto.models.SinglePredictionModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StopsPredictionLazyColumn(
    predictions: List<RoutePredictionsModel>,
    onClickFavoriteItem: (Boolean, RoutePredictionsModel) -> Unit,
    favoriteButtonChecked: @Composable (RoutePredictionsModel) -> Boolean,
    distanceToStop: (RoutePredictionsModel) -> String,
    hideEmptyRoute: Boolean = true,
    isOnStopScreen: Boolean = false
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showScrollButton by remember { mutableStateOf(false) }
    var counter by remember { mutableStateOf(0) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect {
            if(it> 0){
                showScrollButton = true
                delay(800)
                showScrollButton = false
            }

        }
    }

    LaunchedEffect(key1 = counter) {
        delay(1000)
        counter++
    }
    LaunchedEffect(key1 = predictions) {
        counter = 0
    }

    LazyColumn(state = listState) {
        itemsIndexed(predictions) { index, routePredictionItem ->
            if (!hideEmptyRoute || routePredictionItem.directions.isNotEmpty()) {
                StopPredictionCard(
                    routePredictionItem = routePredictionItem,
                    onClick = { },
                    onClickFavorite = { isChecked ->
                        onClickFavoriteItem(isChecked, routePredictionItem)
                    },
                    favoriteButtonChecked = favoriteButtonChecked(routePredictionItem),
                    distanceToStop = { distanceToStop(routePredictionItem) },
                    counter = counter
                )
            }
            if (isOnStopScreen && predictions.size > 1 && index == 0) {
                Text(
                    text = "Other lines at this stop:",
                    modifier = Modifier.padding(horizontal = 10.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
    ScrollToTopButton(
        onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
        showButton = showScrollButton
    )
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
                directions = listOf()
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
        distanceToStop = { "0.2 Km" },
        hideEmptyRoute = true
    )
}