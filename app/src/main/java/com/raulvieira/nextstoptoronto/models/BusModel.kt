package com.raulvieira.nextstoptoronto.models

data class BusModel(
    val id: String,
    val routeTag: String,
    val predictable: String,
    val heading: String,
    val speedKmHr: String,
    val lat: String,
    val lon: String,
    val dirTag: String,
    val secsSinceReport: String
)
