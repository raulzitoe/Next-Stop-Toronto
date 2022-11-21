package com.raulvieira.nextstoptoronto

import com.raulvieira.nextstoptoronto.database.RoomDao
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Repository(private val apiService: RetrofitInterface, private val database: RoomDao) {

    suspend fun getRouteList() = apiService.requestRouteList()
    fun getStopPrediction(stopId: String) =
        flow { emit(apiService.requestStopPrediction(stopId = stopId).body()) }

    fun getRouteConfig(routeTag: String) =
        flow { emit(apiService.requestRouteConfig(routeTag = routeTag).body()) }

    fun getStopPredictionByRoute(routeTag: String, stopTag: String) = flow {
        emit(
            apiService.requestStopPredictionByRoute(routeTag = routeTag, stopTag = stopTag).body())
    }

    suspend fun addToFavorites(item: FavoritesModel) {
        database.addToFavorites(item)
    }

    suspend fun removeFromFavorites(stopTag: String, routeTag: String) {
        database.removeFromFavorites(stopTag = stopTag, routeTag = routeTag)
    }

    fun getItemFromFavorites(stopTag: String, routeTag: String): Flow<FavoritesModel?> {
        return database.getItemFromFavorites(stopTag = stopTag, routeTag = routeTag)
    }

    suspend fun isOnCartDatabase(stopTag: String, routeTag: String) : Flow<Boolean> {
        return database.isOnCartDatabase(stopTag = stopTag, routeTag = routeTag)
    }

}