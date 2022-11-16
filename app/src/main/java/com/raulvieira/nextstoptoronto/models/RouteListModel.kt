package com.raulvieira.nextstoptoronto.models

import com.google.gson.annotations.SerializedName

data class RouteListModel(
    @SerializedName("route")
    val routeList: List<RouteModel>
)

data class RouteModel(
    val tag: String,
    val title: String
)