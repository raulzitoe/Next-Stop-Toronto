package com.raulvieira.nextstoptoronto.screens.nearme

import android.location.Location
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.Repository
import com.raulvieira.nextstoptoronto.models.StopModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearMeViewModel @Inject constructor(private val repository: Repository) : ViewModel(),
    DefaultLifecycleObserver {

    private val _uiState: MutableStateFlow<StopPredictionModel> = MutableStateFlow(StopPredictionModel(
        listOf()
    ))
    val uiState: StateFlow<StopPredictionModel> = _uiState
    private var stopsNearby: List<StopModel> = listOf()
    var userLocation: Location? = null
    var test: Flow<StopPredictionModel?> = flowOf()

    private var job = Job()
        get() {
            if (field.isCancelled) field = Job()
            return field
        }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        cancelScope()
    }

    private fun cancelScope() {
        job.cancel()
    }

    fun getStopsNearby() {
        viewModelScope.launch {
            repository.getStopsFromDatabase().collectLatest { stopsList ->
                val stops: MutableList<StopModel> = mutableListOf()
                stopsList.forEach { stop ->
                    val distance = FloatArray(1)
                    userLocation?.let {
                        Location.distanceBetween(
                            it.latitude,
                            it.longitude,
                            stop.latitude.toDouble(),
                            stop.longitude.toDouble(),
                            distance
                        )
                    }
                    if (distance[0] < 500) {
                        stops.add(stop)
                    }
                }
                stopsNearby = stops
                stopsNearby.forEach { stop ->
                    test = merge(nearMePredictionStream(viewModelScope, stop.stopId), test)
                }
                subscribeToStopStream()
            }
        }
    }

    fun calculateStopDistance(stopTag: String): String {
        val stop = stopsNearby.find { it.stopTag == stopTag }
        val distance = FloatArray(1)
        stop?.let { stopData ->

            userLocation?.let {
                Location.distanceBetween(it.latitude, it.longitude, stopData.latitude.toDouble(), stopData.longitude.toDouble(), distance)
            }
        }
        return "%.1f Km".format(distance[0]/1000)
    }


    private fun subscribeToStopStream() {
        viewModelScope.launch {
            test.collect { data ->
                data?.let { dataNotNull ->
                    _uiState.update { StopPredictionModel(predictions = _uiState.value.predictions + dataNotNull.predictions) }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun nearMePredictionStream(
        scope: CoroutineScope,
        stopId: String
    ): Flow<StopPredictionModel?> {
        return repository.getStopPrediction(stopId).flatMapLatest { stopPrediction ->
            if (stopPrediction == null || stopPrediction.predictions.isEmpty()) return@flatMapLatest flowOf(
                StopPredictionModel(listOf())
            )
            val stopsDataFormatted: MutableList<String> = mutableListOf()
            stopPrediction.predictions.forEach {
                stopsDataFormatted.add(it.routeTag + "|" + it.stopTag)
            }
            repository.requestPredictionsForMultiStops(scope, stopsDataFormatted)
        }
    }
}