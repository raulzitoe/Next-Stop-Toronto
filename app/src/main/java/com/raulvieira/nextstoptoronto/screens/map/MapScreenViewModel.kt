package com.raulvieira.nextstoptoronto.screens.map

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.Repository
import com.raulvieira.nextstoptoronto.RouteConfigModel
import com.raulvieira.nextstoptoronto.StopPredictionModel
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapScreenViewModel @Inject constructor(private val repository: Repository, private val application: Application) : ViewModel() {
    private val _stopState: MutableStateFlow<StopPredictionModel> = MutableStateFlow(StopPredictionModel(arrayListOf()))
    val stopState: StateFlow<StopPredictionModel> = _stopState

    fun getStopPrediction(stopId: String) {
        viewModelScope.launch {
            repository.getStopPrediction(stopId).collect{ stopPredictionData ->
                if (stopPredictionData == null) return@collect
                _stopState.update { stopPredictionData }
            }
        }
    }

    fun getStops(): RouteConfigModel{
        val context = getApplication(application).applicationContext
        val json =
            context.resources.openRawResource(R.raw.stops).bufferedReader().use { it.readText() }
        return Gson().fromJson(json, RouteConfigModel::class.java)
    }
}