package com.raulvieira.nextstoptoronto.screens.map

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.Repository
import com.raulvieira.nextstoptoronto.models.RouteConfigModel
import com.raulvieira.nextstoptoronto.models.StopModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapScreenViewModel @Inject constructor(private val repository: Repository, private val application: Application) : ViewModel() {
    private val _stopState: MutableStateFlow<StopPredictionModel> = MutableStateFlow(
        StopPredictionModel(listOf())
    )
    val stopState: StateFlow<StopPredictionModel> = _stopState

    private val _stopList: MutableStateFlow<List<StopModel>> = MutableStateFlow(arrayListOf())
    val stopList: StateFlow<List<StopModel>> = _stopList

    init {
        getStopsFromDatabase()
        fetchStopsListFromApi()
    }

    fun getStopPrediction(stopId: String) {
        viewModelScope.launch {
            repository.getStopPrediction(stopId).collect{ stopPredictionData ->
                if (stopPredictionData == null) return@collect
                _stopState.update { stopPredictionData }
            }
        }
    }

    fun getStops(): RouteConfigModel {
        val context = getApplication(application).applicationContext
        val json =
            context.resources.openRawResource(R.raw.stops).bufferedReader().use { it.readText() }
        return Gson().fromJson(json, RouteConfigModel::class.java)
    }

    private fun getStopsFromDatabase() {
        viewModelScope.launch {
            repository.getStopsFromDatabase().collect { stopsData ->
                _stopList.update { stopsData }
            }
        }
    }

    private fun fetchStopsListFromApi(){
        //TODO if stops are not that old, don't fetch
        viewModelScope.launch {
            setStopsDatabase(repository.fetchStopsListFromApi())
        }
    }

    private fun setStopsDatabase(stopsList: List<StopModel>) {
        viewModelScope.launch {
            repository.setStopsDatabase(stopsList)
        }
    }
}