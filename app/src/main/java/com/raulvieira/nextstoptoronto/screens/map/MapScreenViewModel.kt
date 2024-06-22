package com.raulvieira.nextstoptoronto.screens.map

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.models.PathModel
import com.raulvieira.nextstoptoronto.repository.Repository
import com.raulvieira.nextstoptoronto.models.StopModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapScreenViewModel @Inject constructor(
    private val repository: Repository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val stopIdFlow: MutableStateFlow<String> = MutableStateFlow("")
    val stopState: StateFlow<StopPredictionModel?> = getStopPrediction()
    private val _stopList: MutableStateFlow<List<StopModel>> = MutableStateFlow(arrayListOf())
    val stopList: StateFlow<List<StopModel>> = _stopList
    private val _paths = MutableStateFlow<List<PathModel>>(listOf())
    val paths = _paths.asStateFlow()

    init {
        val routeTag = savedStateHandle.get<String>("routeTag").orEmpty()

        if (routeTag.isNotBlank()) {
            getPathsAndStopsForRoute(routeTag)
        } else {
            getStopsFromDatabase()
        }
    }

    fun clearStopIdFlow() {
        stopIdFlow.update { "" }
    }

    fun setStopIdValue(stopId: String) {
        stopIdFlow.update { stopId }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getStopPrediction(): StateFlow<StopPredictionModel?> {
        return stopIdFlow.flatMapLatest { stopId ->
            flow {
                emit(null)
                while (true) {
                    if (stopId.isNotBlank()) {
                        try {
                            emit(StopPredictionModel(listOf()))
                            repository.getStopPrediction(stopId).collect { stopPredictionData ->
                                if (stopPredictionData == null) return@collect
                                emit(stopPredictionData)
                            }
                        } catch (e: Exception) {
                            Log.e("Exception", e.toString())
                        }
                        delay(10000)
                    } else {
                        delay(100)
                    }
                }

            }
        }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(), initialValue = StopPredictionModel(
                listOf()
            )
        )
    }

    private fun getPathsAndStopsForRoute(routeTag: String) {
        viewModelScope.launch {
            repository.getRouteConfigForPath(routeTag)
                .onSuccess { response ->
                    _paths.update { response.route.paths }
                    _stopList.update { response.route.stopsList }
                }
                .onFailure {
                    // TODO: Handle error
                }

        }
    }

    // Function to get local stops for testing
//    fun getStops(): RouteConfigModel {
//        val context = getApplication(application).applicationContext
//        val json =
//            context.resources.openRawResource(R.raw.stops).bufferedReader().use { it.readText() }
//        return Gson().fromJson(json, RouteConfigModel::class.java)
//    }

    private fun getStopsFromDatabase() {
        viewModelScope.launch {
            _stopList.update { repository.getStopsFromDatabase() }
        }
    }
}