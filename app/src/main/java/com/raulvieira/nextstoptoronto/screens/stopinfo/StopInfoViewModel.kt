package com.raulvieira.nextstoptoronto.screens.stopinfo

import android.util.Log
import androidx.lifecycle.*
import com.raulvieira.nextstoptoronto.repository.Repository
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class StopInfoViewModel @Inject constructor(
    private val repository: Repository,
    state: SavedStateHandle
) : ViewModel() {

    private val _stopId = state.get<String>("stopId")
    val uiState: StateFlow<StopInfoScreenState> = stopPredictionStream()

    fun handleFavoriteItem(isButtonChecked: Boolean, item: FavoritesModel) {
        viewModelScope.launch {
            if (isButtonChecked) {
                repository.addToFavorites(item)
            } else {
                repository.removeFromFavorites(item.stopTag, item.routeTag)
            }
        }
    }

    fun isRouteFavorited(
        stopTag: String,
        routeTag: String,
        stopTitle: String
    ): SharedFlow<Boolean> = flow {
        repository.isRouteFavorited(stopTag = stopTag, routeTag = routeTag, stopTitle = stopTitle)
            .collect {
                emit(it)
            }
    }.shareIn(viewModelScope, replay = 1, started = SharingStarted.Lazily)

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun stopPredictionStream(): StateFlow<StopInfoScreenState> {
        return repository.getStopPrediction(_stopId ?: "").flatMapLatest { stopPrediction ->
            flow<StopInfoScreenState> {
                stopPrediction?.let { prediction ->
                    val stopsDataFormatted =
                        prediction.predictions.map { it.routeTag + "|" + it.stopTag }
                   while (true) {
                      try {
                          repository.requestPredictionsForMultiStops(stopsDataFormatted)?.let { data ->
                              emit(StopInfoScreenState.Success(data = data))
                          }
                      } catch (e: Exception) {
                          Log.e("Exception", e.toString())
                      }
                       delay(10000)
                   }
               }
            }

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            initialValue = StopInfoScreenState.Loading
        )
    }
}