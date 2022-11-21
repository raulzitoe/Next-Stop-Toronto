package com.raulvieira.nextstoptoronto.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.Repository
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    private val _uiState: MutableStateFlow<StopPredictionModel> =
        MutableStateFlow(StopPredictionModel(arrayListOf()))
    val uiState: StateFlow<StopPredictionModel> = _uiState

    init {
        subscribeToFavorites()
    }

    private fun subscribeToFavorites() {
        viewModelScope.launch {
            val stopsDataFormatted: MutableList<String> = mutableListOf()
            repository.getFavorites().collect { favoritesList ->
                favoritesList.forEach {
                    stopsDataFormatted.add(it.routeTag + "|" + it.stopTag)
                }
                while (currentCoroutineContext().isActive) {
                    repository.requestPredictionsForMultiStops(stopsDataFormatted)
                        .collect { predictions ->
                            if (predictions != null) {
                                _uiState.update { predictions }
                            }
                        }
                    delay(10000)
                }
            }
        }
    }
}