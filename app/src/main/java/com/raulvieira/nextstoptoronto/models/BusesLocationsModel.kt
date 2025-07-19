package com.raulvieira.nextstoptoronto.models

import com.google.gson.annotations.SerializedName

data class BusesLocationsModel(
    val lastTime: TimeModel,
    @SerializedName("vehicle")
    val buses: List<BusModel>
)

data class TimeModel(
    val time: String
)
