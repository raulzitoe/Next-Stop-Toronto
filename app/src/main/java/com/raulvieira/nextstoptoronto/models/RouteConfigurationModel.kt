package com.raulvieira.nextstoptoronto.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class RouteConfigurationModel(
    val route: RouteModel
)

data class RouteModel(
    val title: String, // Ex: 29-Dufferin
    @SerializedName("stop")
    val stopsList: List<StopModel>
    // Theres also direction field, not used for now
)

@Entity(tableName = "stops_table")
data class StopModel(
    @PrimaryKey @SerializedName("tag") val stopTag: String = "-1", // Ex: 15466 -> Unique stop ID
    val stopId: String = "", // Id but not unique -> Stations
    @SerializedName("lat")
    val latitude: String = "",
    @SerializedName("lon")
    val longitude: String = "",
    val title: String = "" // Ex: Dufferin St At Wilson Ave South Side
)

