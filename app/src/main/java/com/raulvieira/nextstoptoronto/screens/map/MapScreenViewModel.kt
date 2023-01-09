package com.raulvieira.nextstoptoronto.screens.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.database.Repository
import com.raulvieira.nextstoptoronto.models.StopModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MapScreenViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
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

    // Function to get local stops for testing
//    fun getStops(): RouteConfigModel {
//        val context = getApplication(application).applicationContext
//        val json =
//            context.resources.openRawResource(R.raw.stops).bufferedReader().use { it.readText() }
//        return Gson().fromJson(json, RouteConfigModel::class.java)
//    }

    private fun getStopsFromDatabase() {
        viewModelScope.launch {
            repository.getStopsFromDatabase().collect { stopsData ->
                _stopList.update { stopsData }
            }
        }
    }

    private fun fetchStopsListFromApi(){
        viewModelScope.launch {
            val lastUpdated = repository.getLastUpdatedDate()
            val currentDate = Calendar.getInstance().time
            if (lastUpdated != null) {
                val dateToCheck = Calendar.getInstance()
                dateToCheck.time = lastUpdated
                dateToCheck.add(Calendar.DATE, 60)
                // Fetch data if 60 days since last update
                if(dateToCheck.time < currentDate) {
                    Log.i("fetch_stops", "Fetching stops because local data is old")
                    setStopsDatabase(repository.fetchStopsListFromApi())
                    repository.setLastUpdatedDate(Calendar.getInstance().time)
                }
                else {
                    Log.i("fetch_stops", "Stop data is not old, not fetching")
                }
            }
            else{
                Log.i("fetch_stops", "Fetching stops because local data is null")
                setStopsDatabase(repository.fetchStopsListFromApi())
                repository.setLastUpdatedDate(Calendar.getInstance().time)
            }


        }
    }

    private fun setStopsDatabase(stopsList: List<StopModel>) {
        viewModelScope.launch {
            repository.setStopsDatabase(stopsList)
            val currentTime: Date = Calendar.getInstance().time
            repository.setLastUpdatedDate(currentTime)
        }
    }
}