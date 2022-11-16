package com.raulvieira.nextstoptoronto.models

data class StopPredictionModel(
    val predictions: ArrayList<RoutePredictionsModel>
)

data class RoutePredictionsModel(
    val routeTag: String,
    val stopTag: String,
    val routeTitle: String,
    val stopTitle: String,
    val direction: ArrayList<PredictionModel>
)

data class PredictionModel(
    val title: String,
    val prediction: ArrayList<SinglePredictionModel>
)

data class SinglePredictionModel(
    val seconds: String,
    val minutes: String
)
