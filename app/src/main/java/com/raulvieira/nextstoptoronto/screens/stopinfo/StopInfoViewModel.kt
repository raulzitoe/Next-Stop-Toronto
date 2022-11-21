package com.raulvieira.nextstoptoronto.screens.stopinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.Repository
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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

    fun handleFavoriteItem(isButtonChecked: Boolean, item: FavoritesModel) {
        viewModelScope.launch {
            if (isButtonChecked) {
                repository.addToFavorites(item)
            } else {
                repository.removeFromFavorites(item.stopTag, item.routeTag)
            }
        }
    }

    suspend fun isRouteFavorited(stopTag: String, routeTag: String, stopTitle: String): StateFlow<Boolean> = flow {
        repository.isOnCartDatabase(stopTag = stopTag, routeTag = routeTag, stopTitle = stopTitle).collect {
            emit(it)
        }
    }.stateIn(viewModelScope)


    suspend fun getItemFromFavorites(stopTag: String, routeTag: String, stopTitle: String) =
        repository.getItemFromFavorites(stopTag = stopTag, routeTag = routeTag, stopTitle = stopTitle)
            .stateIn(viewModelScope)


}