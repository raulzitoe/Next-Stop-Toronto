package com.raulvieira.nextstoptoronto

data class RouteConfigModel(
    val route: Route2Model
)

data class Route2Model(
    val stop: ArrayList<StopsModel>
)

data class StopsModel(
    val stopId: String,
    val tag: String,
    val lat: String,
    val lon: String,
    val title: String
)
