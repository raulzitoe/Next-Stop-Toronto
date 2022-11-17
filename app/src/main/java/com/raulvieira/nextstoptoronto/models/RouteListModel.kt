package com.raulvieira.nextstoptoronto.models

import com.google.gson.annotations.SerializedName

data class RouteListModel(
    @SerializedName("route")
    val routeList: List<RouteLineModel>
)

data class RouteLineModel(
    @SerializedName("tag")
    val routeTag: String,
    val title: String
)