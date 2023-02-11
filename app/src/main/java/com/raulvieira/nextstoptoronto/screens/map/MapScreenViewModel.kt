package com.raulvieira.nextstoptoronto.screens.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class MapScreenViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val stopIdFlow: MutableStateFlow<String> = MutableStateFlow("")
    val stopState: StateFlow<StopPredictionModel?> = getStopPrediction()
    private val _stopList: MutableStateFlow<List<StopModel>> = MutableStateFlow(arrayListOf())
    val stopList: StateFlow<List<StopModel>> = _stopList

    init {
        getStopsFromDatabase()
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