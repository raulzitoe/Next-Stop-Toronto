package com.raulvieira.nextstoptoronto.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import com.raulvieira.nextstoptoronto.models.SinglePredictionModel

@Composable
fun StopsLazyColumn(
    predictions: List<RoutePredictionsModel>,
    onClickFavoriteItem: (Boolean, RoutePredictionsModel) -> Unit,
    favoriteButtonChecked: @Composable (RoutePredictionsModel) -> Boolean,
    distanceToStop: (RoutePredictionsModel) -> String
) {
    LazyColumn {
        items(predictions) { routePredictionItem ->
            StopPredictionCard(
                routePredictionItem = routePredictionItem,
                onClick = { },
                onClickFavorite = { isChecked ->
                    onClickFavoriteItem(isChecked, routePredictionItem)
                },
                favoriteButtonChecked = favoriteButtonChecked(routePredictionItem),
                distanceToStop = { distanceToStop(routePredictionItem) }
            )
        }
    }
}

@Preview
@Composable
fun StopsLazyColumnPreview() {
    StopsLazyColumn(
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
        distanceToStop = { "0.2 Km" }
    )
}