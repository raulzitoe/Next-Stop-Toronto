package com.raulvieira.nextstoptoronto.database

import com.raulvieira.nextstoptoronto.models.DateDatabaseModel
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.StopModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

class Repository(private val apiService: RetrofitInterface, private val database: RoomDao) {

    // API RELATED
    suspend fun getRouteList() = apiService.requestRouteList()

    fun getStopPrediction(stopId: String) =
        flow { emit(apiService.requestStopPrediction(stopId = stopId).body()) }

    fun getRouteConfig(routeTag: String) =
        flow { emit(apiService.requestRouteConfig(routeTag = routeTag).body()) }

    fun requestPredictionsForMultiStops(
        scope: CoroutineScope,
        stops: List<String>
    ): Flow<StopPredictionModel?> {
        return flow {
            while (scope.isActive) {
                emit(apiService.requestPredictionsForMultiStops(stops = stops).body())
                delay(10000)
            }
        }
    }

    suspend fun fetchStopsListFromApi(): List<StopModel> {
        val stopsList: MutableList<StopModel> = listOf<StopModel>().toMutableList()
        val routes = apiService.requestRouteList()
//        var count = 0 //TODO Remove count, it is used just to limit api usage for testing
        routes.body()?.routeList?.forEach {
//            if(count==3) return@forEach
            if (it.routeTag == "41") {
                val data = apiService.requestRouteConfig(routeTag = it.routeTag).body()
                if (data != null) {
                    stopsList.addAll(data.route.stopsList)
                    return@forEach
                }
            }
//            count++
//            delay(500)
        }
        stopsList.distinctBy { it.stopTag }
        return stopsList
    }

    // ROOM RELATED
    suspend fun addToFavorites(item: FavoritesModel) {
        database.addToFavorites(item)
    }

    suspend fun removeFromFavorites(stopTag: String, routeTag: String) {
        database.removeFromFavorites(stopTag = stopTag, routeTag = routeTag)
    }

    fun isRouteFavorited(
        stopTag: String,
        routeTag: String,
        stopTitle: String
    ): Flow<Boolean> {
        return database.isRouteFavorited(
            stopTag = stopTag,
            routeTag = routeTag,
            stopTitle = stopTitle
        )
    }

    fun getFavorites() = database.getFavorites()

    fun isFavoritesEmpty():Flow<Boolean> = database.isFavoritesEmpty()

    fun getStopsFromDatabase(): Flow<List<StopModel>> {
        return database.getStopsFromDatabase()
    }

    suspend fun setStopsDatabase(stopsList: List<StopModel>) {
        stopsList.forEach {
            database.insertStopToDatabase(it)
        }
    }

    suspend fun getLastUpdatedDate(): Date? {
        return database.getLastUpdatedDate()?.lastUpdated
    }

    suspend fun setLastUpdatedDate(date: Date) {
        database.setLastUpdatedDate(DateDatabaseModel(lastUpdated = date))
    }

}