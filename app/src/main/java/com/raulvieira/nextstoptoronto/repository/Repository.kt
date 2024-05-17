package com.raulvieira.nextstoptoronto.repository

import android.util.Log
import com.raulvieira.nextstoptoronto.models.DateDatabaseModel
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.StopModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import kotlinx.coroutines.flow.*
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class Repository @Inject constructor(private val apiService: RetrofitInterface, private val database: RoomDao) {

    // API RELATED
    suspend fun getRouteList() = apiService.requestRouteList()

    fun getStopPrediction(stopId: String) =
        flow { emit(apiService.requestStopPrediction(stopId = stopId).body()) }

    fun getRouteConfig(routeTag: String) =
        flow {
            try {
                emit(apiService.requestRouteConfig(routeTag = routeTag).body())
            } catch (e: Exception) {
                Log.e("EXCEPTION", e.message.toString())
            }
        }

    suspend fun requestPredictionsForMultiStops(stops: List<String>): StopPredictionModel? {
        return if (stops.isNotEmpty()) {
            apiService.requestPredictionsForMultiStops(stops = stops).body()
        } else null
    }

    suspend fun fetchStopsListFromApi(onPercentageCompletion: (Float) -> Unit): List<StopModel> {
        val stopsList: MutableList<StopModel> = listOf<StopModel>().toMutableList()
        val routes = apiService.requestRouteList().body()
        val routesSize = (routes?.routeList?.size ?: 0).toFloat()
        routes?.routeList?.forEachIndexed { index, route ->
            onPercentageCompletion((index + 1) / routesSize)
            val data = apiService.requestRouteConfig(routeTag = route.routeTag).body()
            data?.let { stopsList.addAll(it.route.stopsList) }
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

    fun isFavoritesEmpty(): Flow<Boolean> = database.isFavoritesEmpty()

    suspend fun getStopsFromDatabase(): List<StopModel> {
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