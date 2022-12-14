package com.raulvieira.nextstoptoronto.models

import com.google.gson.annotations.SerializedName

data class StopPredictionModel(
    val predictions: List<RoutePredictionsModel>
)

data class RoutePredictionsModel(
    val routeTag: String, // Ex: 189
    val stopTag: String, // Stop unique number
    val routeTitle: String, // 189-Stockyards
    val stopTitle: String, // Ex: Keele St At Glenlake Ave
    @SerializedName("dirTitleBecauseNoPredictions")
    val directionTitleWhenNoPredictions: String,
    @SerializedName("direction")
    val directions: List<PredictionModel>
)

data class PredictionModel(
    val title: String, // Ex: West - 189 Stockyards towards Scarlett Rd
    @SerializedName("prediction")
    val predictions: List<SinglePredictionModel>
)

data class SinglePredictionModel(
    val branch: String, // Line number Ex: 189
    val vehicle: String, // Vehicle unique number
    val seconds: String,
    val minutes: String
)