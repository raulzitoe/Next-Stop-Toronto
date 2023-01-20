package com.raulvieira.nextstoptoronto.screens.home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.database.Repository
import com.raulvieira.nextstoptoronto.models.RouteListModel
import com.raulvieira.nextstoptoronto.models.StopModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _uiState: MutableStateFlow<RouteListModel> = MutableStateFlow(
        RouteListModel(
            emptyList()
        )
    )
    val uiState: StateFlow<RouteListModel> = _uiState
    var updatePercentage = mutableStateOf(0.0f)
    var showDialog = mutableStateOf(false)

    init {
        getRouteList()
        updateStopsList(
            onPercentageCompletion = { percentage ->
                updatePercentage.value = percentage
            },
            isUpdating = { isUpdating -> showDialog.value = isUpdating })
    }

    private fun getRouteList() {
        viewModelScope.launch {
            repository.getRouteList().body()?.let { routes ->
                _uiState.update { routes }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun shouldUpdateStopsListDatabase(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            viewModelScope.launch {
                val lastUpdated = repository.getLastUpdatedDate()
                val currentDate = Calendar.getInstance().time
                if (lastUpdated != null) {
                    val dateToCheck = Calendar.getInstance()
                    dateToCheck.time = lastUpdated
                    dateToCheck.add(Calendar.DATE, 90)
                    // Fetch data if 90 days since last update
                    if (dateToCheck.time < currentDate) {
                        Log.i("fetch_stops", "Fetching stops because local data is old")
                        continuation.resume(true, onCancellation = null)
                    } else {
                        Log.i("fetch_stops", "Stop data is not old, not fetching")
                        continuation.resume(false, onCancellation = null)
                    }
                } else {
                    Log.i(
                        "fetch_stops",
                        "Last update data is null, first time using app," +
                                " using local pre-populated data for now"
                    )
                    repository.setLastUpdatedDate(Calendar.getInstance().time)
                    continuation.resume(false, onCancellation = null)
                }
            }
        }
    }

    private fun updateStopsList(
        onPercentageCompletion: (Float) -> Unit,
        isUpdating: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (shouldUpdateStopsListDatabase()) {
                isUpdating(true)
                setStopsDatabase(repository.fetchStopsListFromApi(onPercentageCompletion = { percentage ->
                    onPercentageCompletion(percentage)
                }))
                repository.setLastUpdatedDate(Calendar.getInstance().time)
                isUpdating(false)
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