package com.raulvieira.nextstoptoronto.models

import com.google.gson.annotations.SerializedName

data class RouteConfigModel(
    val route: RouteModel
)

data class RouteModel(
    val title: String, // Ex: 29-Dufferin
    @SerializedName("stop")
    val stopsList: ArrayList<StopsModel>
    // Theres also direction field, not used for now
)

data class StopsModel(
    val stopId: String, // Id but not unique -> Stations
    @SerializedName("tag")
    val stopTag: String, // Ex: 15466 -> Unique stop ID
    @SerializedName("lat")
    val latitude: String,
    @SerializedName("lon")
    val longitude: String,
    val title: String // Ex: Dufferin St At Wilson Ave South Side
)

