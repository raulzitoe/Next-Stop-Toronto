package com.raulvieira.nextstoptoronto

import com.raulvieira.nextstoptoronto.database.RoomDao
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive

class Repository(private val apiService: RetrofitInterface, private val database: RoomDao) {

    suspend fun getRouteList() = apiService.requestRouteList()
    fun getStopPrediction(stopId: String) =
        flow { emit(apiService.requestStopPrediction(stopId = stopId).body()) }

    fun getRouteConfig(routeTag: String) =
        flow { emit(apiService.requestRouteConfig(routeTag = routeTag).body()) }

    fun getStopPredictionByRoute(routeTag: String, stopTag: String) = flow {
        emit(
            apiService.requestStopPredictionByRoute(routeTag = routeTag, stopTag = stopTag).body()
        )
    }

    suspend fun addToFavorites(item: FavoritesModel) {
        database.addToFavorites(item)
    }

    suspend fun removeFromFavorites(stopTag: String, routeTag: String) {
        database.removeFromFavorites(stopTag = stopTag, routeTag = routeTag)
    }

    fun getItemFromFavorites(
        stopTag: String,
        routeTag: String,
        stopTitle: String
    ): Flow<FavoritesModel?> {
        return database.getItemFromFavorites(
            stopTag = stopTag,
            routeTag = routeTag,
            stopTitle = stopTitle
        )
    }

    fun isOnCartDatabase(
        stopTag: String,
        routeTag: String,
        stopTitle: String
    ): Flow<Boolean> {
        return database.isOnCartDatabase2(
            stopTag = stopTag,
            routeTag = routeTag,
            stopTitle = stopTitle
        )
    }

    fun isOnCartDatabase2(
        stopTag: String,
        routeTag: String,
        stopTitle: String
    ): Flow<Boolean> {
        return database.isOnCartDatabase2(
            stopTag = stopTag,
            routeTag = routeTag,
            stopTitle = stopTitle
        )
    }

    fun getFavorites() = database.getFavorites()

    fun requestPredictionsForMultiStops(stops: List<String>): Flow<StopPredictionModel?> {
        return flow {
            emit(apiService.requestPredictionsForMultiStops(stops = stops).body())
        }
    }

}