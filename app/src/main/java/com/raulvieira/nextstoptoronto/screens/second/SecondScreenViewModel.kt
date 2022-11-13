package com.raulvieira.nextstoptoronto.screens.second

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.Repository
import com.raulvieira.nextstoptoronto.StopPredictionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecondScreenViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
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
}