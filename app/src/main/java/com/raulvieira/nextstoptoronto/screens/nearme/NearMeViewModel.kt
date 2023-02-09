package com.raulvieira.nextstoptoronto.screens.nearme

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.repository.Repository
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import com.raulvieira.nextstoptoronto.models.StopModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NearMeViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private var stops: List<StopModel> = listOf()
    var userLocation: Location? = null
    var userLocationFlow: MutableSharedFlow<Location> = MutableSharedFlow()
    val uiState: StateFlow<NearMeScreenState> = subscribeToStopStream()

    fun calculateStopDistance(stopTag: String): String {
        val stop = stops.find { it.stopTag == stopTag }
        val distance = FloatArray(1)
        stop?.let { stopData ->
            userLocation?.let {
                Location.distanceBetween(
                    it.latitude,
                    it.longitude,
                    stopData.latitude.toDouble(),
                    stopData.longitude.toDouble(),
                    distance
                )
            }
        }
        return "%.1f Km".format(distance[0] / 1000)
    }

    fun subscribeToStopStream(): StateFlow<NearMeScreenState> {

        return userLocationFlow.flatMapLatest { userLocationData ->
            userLocation = userLocationData
            val stopsList = repository.getStopsFromDatabase()
            stops = stopsList
            val stopsNearby: MutableList<StopModel> = mutableListOf()
            val flowList: MutableList<Flow<StopPredictionModel>> = mutableListOf()
            stopsList.forEach { stop ->
                val distance = FloatArray(1)
                Location.distanceBetween(
                    userLocationData.latitude,
                    userLocationData.longitude,
                    stop.latitude.toDouble(),
                    stop.longitude.toDouble(),
                    distance
                )
                if (distance[0] < 500) {
                    stopsNearby.add(stop)
                }
            }
            stopsNearby.forEach { stop ->
                if (stop.stopId.isNotEmpty()) {
                    flowList.add(nearMePredictionStream(stop.stopId))
                }
            }
            channelFlow<NearMeScreenState> {
                combine(*flowList.toTypedArray()) { combinedData ->
                    val predictions: MutableList<RoutePredictionsModel> = mutableListOf()
                    combinedData.forEach {
                        predictions.addAll(it.predictions)
                    }
                    StopPredictionModel(predictions = predictions)
                }.collectLatest { stopData ->
                    send(NearMeScreenState.Success(data = stopData))
                }

            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            initialValue = NearMeScreenState.Loading
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun nearMePredictionStream(
        stopId: String
    ): Flow<StopPredictionModel> {
        return repository.getStopPrediction(stopId).flatMapLatest { stopPrediction ->
            flow {
                stopPrediction?.let { prediction ->
                    val stopsDataFormatted =
                        prediction.predictions.map { it.routeTag + "|" + it.stopTag }
                    while (true) {
                        try {
                            repository.requestPredictionsForMultiStops(stopsDataFormatted)
                                ?.let { data ->
                                    emit(data)
                                }
                        } catch (e: Exception) {
                            Log.e("Exception", e.toString())
                        }
                        delay(10000)
                    }
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            initialValue = StopPredictionModel(listOf())
        )
    }

    fun isRouteFavorited(
        stopTag: String,
        routeTag: String,
        stopTitle: String
    ): SharedFlow<Boolean> = flow {
        repository.isRouteFavorited(
            stopTag = stopTag,
            routeTag = routeTag,
            stopTitle = stopTitle
        ).collect {
            emit(it)
        }
    }.shareIn(viewModelScope, replay = 1, started = SharingStarted.Lazily)

    fun handleFavoriteItem(isButtonChecked: Boolean, item: FavoritesModel) {
        viewModelScope.launch {
            if (isButtonChecked) {
                repository.addToFavorites(item)
            } else {
                repository.removeFromFavorites(item.stopTag, item.routeTag)
            }
        }
    }
}