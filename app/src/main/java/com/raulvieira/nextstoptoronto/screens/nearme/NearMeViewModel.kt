package com.raulvieira.nextstoptoronto.screens.nearme

import android.location.Location
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.repository.Repository
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.RoutePredictionsModel
import com.raulvieira.nextstoptoronto.models.StopModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NearMeViewModel @Inject constructor(private val repository: Repository) : ViewModel(),
    DefaultLifecycleObserver {

    private val _uiState: MutableStateFlow<NearMeScreenState> = MutableStateFlow(
        NearMeScreenState.Loading
    )
    val uiState: StateFlow<NearMeScreenState> = _uiState
    private var stops: List<StopModel> = listOf()
    var userLocation: Location? = null
    var userLocationFlow: MutableSharedFlow<Location> = MutableSharedFlow()

    private var job = Job()
        get() {
            if (field.isCancelled) field = Job()
            return field
        }

    fun cancelSubscription() {
        userLocation = null
        job.cancel()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        job.cancel()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        subscribeToStopStream()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (!job.isActive) {
            subscribeToStopStream()
        }
    }

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

    fun subscribeToStopStream() {
        viewModelScope.launch(job) {
            try {
                userLocationFlow.collectLatest { userLocationData ->
                    userLocation = userLocationData
                    repository.getStopsFromDatabase().collect { stopsList ->
                        stops = stopsList
                        val stopsNearby: MutableList<StopModel> = mutableListOf()
                        val flowList: MutableList<Flow<StopPredictionModel?>> = mutableListOf()
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
                                flowList.add(nearMePredictionStream(viewModelScope, stop.stopId))
                            }
                        }
                        combine(*flowList.toTypedArray()) { combinedData ->
                            val predictions: MutableList<RoutePredictionsModel> = mutableListOf()
                            combinedData.forEach {
                                it?.predictions?.let { it2 ->
                                    predictions.addAll(it2)
                                }

                            }
                            StopPredictionModel(predictions = predictions)
                        }.collect { stopData ->
                            _uiState.update { NearMeScreenState.Success(data = stopData) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("EXCEPTION", e.message.toString())
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun nearMePredictionStream(
        scope: CoroutineScope,
        stopId: String
    ): Flow<StopPredictionModel?> {
        return repository.getStopPrediction(stopId).flatMapLatest { stopPrediction ->
            if (stopPrediction == null || stopPrediction.predictions.isEmpty()) {
                return@flatMapLatest flowOf(StopPredictionModel(listOf()))
            }
            val stopsDataFormatted =
                stopPrediction.predictions.map { it.routeTag + "|" + it.stopTag }
            repository.requestPredictionsForMultiStops(scope, stopsDataFormatted)
        }
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