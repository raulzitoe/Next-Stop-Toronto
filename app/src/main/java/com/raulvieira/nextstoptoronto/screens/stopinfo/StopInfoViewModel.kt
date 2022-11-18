package com.raulvieira.nextstoptoronto.screens.stopinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.Repository
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StopInfoViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _uiState: MutableStateFlow<StopPredictionModel> = MutableStateFlow(
        StopPredictionModel(predictions = arrayListOf())
    )
    val uiState: StateFlow<StopPredictionModel> = _uiState


    fun getStopPrediction(stopId: String) {
        if (stopId.isNotBlank()) {
            viewModelScope.launch {
                repository.getStopPrediction(stopId).collect { data ->
                    if (data == null) return@collect
                    _uiState.update { data }
                }
            }
        }
    }
}