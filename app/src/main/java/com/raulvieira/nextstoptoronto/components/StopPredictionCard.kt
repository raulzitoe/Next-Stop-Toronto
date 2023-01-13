package com.raulvieira.nextstoptoronto.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FollowTheSigns
import androidx.compose.material.icons.outlined.Signpost
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import com.raulvieira.nextstoptoronto.models.SinglePredictionModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopPredictionCard(
    routePredictionItem: RoutePredictionsModel,
    onClick: (String) -> Unit,
    onClickFavorite: (Boolean) -> Unit,
    favoriteButtonChecked: Boolean,
    distanceToStop: () -> String
) {
    Card(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(10.dp), onClick = { }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(end = 10.dp)
                                .align(Alignment.CenterStart),
                            color = Color.Red,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = routePredictionItem.routeTitle,
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(vertical = 5.dp, horizontal = 5.dp),
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (distanceToStop().isNotEmpty()) {

                        Surface(
                            modifier = Modifier
                                .wrapContentSize(),
                            color = Color.Red,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = distanceToStop(),
                                modifier = Modifier.padding(vertical = 5.dp, horizontal = 5.dp),
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                fontSize = 16.sp
                            )

                        }

                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.FollowTheSigns,
                        contentDescription = "Localized description"
                    )
                    Text(routePredictionItem.stopTitle)
                    FavoriteStopButton(
                        onChecked = { checkedValue -> onClickFavorite(checkedValue) },
                        isChecked = favoriteButtonChecked
                    )
                }
                routePredictionItem.directions.forEach { predictionItem ->
                    Row {
                        Icon(Icons.Outlined.Signpost, contentDescription = "Localized description")
                        Text(predictionItem.title)
                    }
                    predictionItem.predictions.forEach { stopPrediction ->
                        Row {
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
}

@Composable
fun FavoriteStopButton(isChecked: Boolean, onChecked: (Boolean) -> Unit) {
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

@Preview
@Composable
fun StopPredictionCardPreview() {
    StopPredictionCard(
        RoutePredictionsModel(
            routeTag = "99",
            stopTag = "123",
            routeTitle = "99 - Crazy Street aaaasd",
            stopTitle = "Some Rd at Some Avenue",
            directionTitleWhenNoPredictions = "99 - Street short turn",
            directions = listOf(
                PredictionModel(
                    title = "North 99 -  Something towards some Station",
                    predictions = listOf(
                        SinglePredictionModel(
                            branch = "99",
                            vehicle = "1234",
                            minutes = "2",
                            seconds = "120"
                        ),
                        SinglePredictionModel(
                            branch = "99",
                            vehicle = "4321",
                            minutes = "4",
                            seconds = "240"
                        )
                    )
                ),
                PredictionModel(
                    title = "North 99 -  Something towards some Station short turn",
                    predictions = listOf(
                        SinglePredictionModel(
                            branch = "99",
                            vehicle = "1234",
                            minutes = "2",
                            seconds = "120"
                        ),
                        SinglePredictionModel(
                            branch = "99",
                            vehicle = "4321",
                            minutes = "4",
                            seconds = "240"
                        )
                    )
                )
            )
        ),
        onClick = {},
        onClickFavorite = {},
        favoriteButtonChecked = true,
        distanceToStop = { "0.1 Km" }
    )
}