package com.raulvieira.nextstoptoronto.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun StopPredictionCard(
    routePredictionItem: RoutePredictionsModel,
    onClick: () -> Unit,
    onClickFavorite: (Boolean) -> Unit,
    favoriteButtonChecked: Boolean,
    distanceToStop: () -> String,
    counter: Int
) {
    Card(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(10.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                color = Color.Red
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = routePredictionItem.routeTitle,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp, end = 5.dp),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (isSystemInDarkTheme()) LocalContentColor.current else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (distanceToStop().isNotEmpty()) {
                                Surface(
                                    modifier = Modifier.wrapContentSize(),
                                    color = if (isSystemInDarkTheme()) Color.DarkGray else MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = distanceToStop(),
                                        modifier = Modifier.padding(5.dp),
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 16.sp,
                                        color = LocalContentColor.current
                                    )
                                }
                            }
                            FavoriteStopButton(
                                onChecked = { checkedValue -> onClickFavorite(checkedValue) },
                                isChecked = favoriteButtonChecked
                            )
                        }
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.FollowTheSigns,
                    contentDescription = "Localized description"
                )
                Text(
                    text = routePredictionItem.stopTitle,
                    modifier = Modifier.padding(horizontal = 5.dp),
                    fontWeight = FontWeight.ExtraBold,
                )

            }
            Divider(modifier = Modifier.padding(top = 5.dp))
            routePredictionItem.directions.forEach { predictionItem ->
                Row(
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Signpost, contentDescription = "Localized description")
                    Text(predictionItem.title)
                }
                predictionItem.predictions.forEach { stopPrediction ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 20.dp)
                    ) {
                        val predictionSeconds = stopPrediction.seconds.toInt() - counter
                        val minutes = predictionSeconds / 60
                        val seconds = predictionSeconds % 60
                        Icon(
                            Icons.Outlined.DepartureBoard,
                            contentDescription = "Localized description"
                        )
                        Text(text = "#" + stopPrediction.vehicle + " - ")
                        if (predictionSeconds >= 0) {
                            Text(text = "In %02d:%02d".format(minutes, seconds))
                        } else {
                            Text(text = "Now")
                        }
                    }
                }
            }
            if (routePredictionItem.directions.isEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Warning, contentDescription = "Localized description")
                    Text(
                        text = "No predictions at this moment",
                        modifier = Modifier.padding(horizontal = 5.dp),
                        fontSize = 14.sp
                    )
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
            routeTitle = "99 - Crazy Street",
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
        distanceToStop = { "0.1 Km" },
        counter = 0
    )
}