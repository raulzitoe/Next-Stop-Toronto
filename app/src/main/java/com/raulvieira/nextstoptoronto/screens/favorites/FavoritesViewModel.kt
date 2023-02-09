package com.raulvieira.nextstoptoronto.screens.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.repository.Repository
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    private val favoritesPrediction: StateFlow<StopPredictionModel> = stopPredictionStream()
    private val isFavoriteEmpty: StateFlow<Boolean> = repository.isFavoritesEmpty()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val uiState: StateFlow<FavoritesScreenState> =
        combine(isFavoriteEmpty, favoritesPrediction) { isFavoriteEmpty, favoritesPrediction ->
            if (isFavoriteEmpty) {
                FavoritesScreenState.Success(data = StopPredictionModel(listOf()))
            } else {
                FavoritesScreenState.Success(data = favoritesPrediction)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), FavoritesScreenState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun stopPredictionStream(): StateFlow<StopPredictionModel> =
        repository.getFavorites().flatMapLatest { favorites ->
            flow {
                val stopsDataFormatted = favorites.map { it.routeTag + "|" + it.stopTag }
                while (true) {
                    try {
                        repository.requestPredictionsForMultiStops(stopsDataFormatted)
                            ?.let { favoritesPredictions ->
                                emit(favoritesPredictions)
                            }

                    } catch (e: Exception) {
                        Log.e("Exception", e.toString())
                    }
                    delay(10000)
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            initialValue = StopPredictionModel(listOf())
        )


    fun isRouteFavorited(
        stopTag: String,
        routeTag: String,
        stopTitle: String
    ): SharedFlow<Boolean> = flow {
        repository.isRouteFavorited(
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
                repository.removeFromFavorites(item.stopTag, item.routeTag)
            }
        }
    }
}