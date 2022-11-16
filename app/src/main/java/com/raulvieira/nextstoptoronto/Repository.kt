package com.raulvieira.nextstoptoronto

import kotlinx.coroutines.flow.flow

class Repository(private val apiService: RetrofitInterface) {

    suspend fun getRouteList() = apiService.requestRouteList()
    fun getStopPrediction(stopId: String) = flow { emit(apiService.requestStopPrediction(stopId = stopId).body()) }
    fun getRouteConfig(routeTag: String) = flow { emit(apiService.requestRouteConfig(routeTag = routeTag).body()) }

}