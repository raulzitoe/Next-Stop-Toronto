package com.raulvieira.nextstoptoronto.screens.favorites


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.Repository
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    private val _uiState: MutableStateFlow<StopPredictionModel> =
        MutableStateFlow(StopPredictionModel(arrayListOf()))
    val uiState: StateFlow<StopPredictionModel> = _uiState
    private val favoriteRoutes: MutableStateFlow<List<String>> = MutableStateFlow(arrayListOf())
    private var job = Job()
        get() {
            if (field.isCancelled) field = Job()
            return field
        }

    fun cancelScope() {
        job.cancel()
    }

    fun subscribeToFavorites() {
        viewModelScope.launch(job) {
            repository.getFavorites().collect { favoritesList ->
                val stopsDataFormatted: MutableList<String> = mutableListOf()

                favoritesList.forEach {
                    stopsDataFormatted.add(it.routeTag + "|" + it.stopTag)
                }
                favoriteRoutes.update { stopsDataFormatted }
            }
        }

        viewModelScope.launch(job) {
            favoriteRoutes.collect {
                while (isActive) {
                    var test: StopPredictionModel? = null
                        repository.requestPredictionsForMultiStops(favoriteRoutes.value)
                            .collect { predictions ->
                                test = predictions
                                if (predictions != null) {
                                    _uiState.update { predictions }
                                }
                            }
                    if(!test?.predictions.isNullOrEmpty()){
                        delay(10000)
                    }
                }
            }


        }
    }

    fun isRouteFavorited(
        stopTag: String,
        routeTag: String,
        stopTitle: String
    ): SharedFlow<Boolean> = flow {
        repository.isOnCartDatabase2(
            stopTag = stopTag,
            routeTag = routeTag,
            stopTitle = stopTitle
        ).collect {
            emit(it)
        }
    }.shareIn(viewModelScope, replay = 1, started = SharingStarted.Lazily)

    fun handleFavoriteItem(isButtonChecked: Boolean, item: FavoritesModel) {
        viewModelScope.launch {
            if (isButtonChecked) {
                repository.addToFavorites(item)
            } else {
                val myList =
                    _uiState.value.predictions.filter { value -> (value.routeTag != item.routeTag && value.stopTag != item.stopTag && value.stopTitle != item.stopTitle) }
                _uiState.update { StopPredictionModel(ArrayList(myList)) }
                repository.removeFromFavorites(item.stopTag, item.routeTag)
            }
        }
    }
}