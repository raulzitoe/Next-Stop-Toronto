package com.raulvieira.nextstoptoronto

import kotlinx.coroutines.flow.flow

class Repository(private val apiService: RetrofitInterface) {

    suspend fun getRouteList() = apiService.requestRouteList()
    suspend fun getStopPrediction(stopId: String) = flow { emit(apiService.requestStopPrediction(stopId = stopId).body()) }

}