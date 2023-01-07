package com.raulvieira.nextstoptoronto.screens.stopinfo

import androidx.lifecycle.*
import com.raulvieira.nextstoptoronto.Repository
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.models.StopPredictionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StopInfoViewModel @Inject constructor(
    private val repository: Repository,
    state: SavedStateHandle
) : ViewModel(),
    DefaultLifecycleObserver {

    private val _uiState: MutableStateFlow<StopPredictionModel> = MutableStateFlow(
        StopPredictionModel(predictions = listOf())
    )
    val uiState: StateFlow<StopPredictionModel> = _uiState
    private var job = Job()
        get() {
            if (field.isCancelled) field = Job()
            return field
        }
    private val _stopId = state.get<String>("stopId")

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        subscribeToStopStream()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        job.cancel()
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
    private fun stopPredictionStream(scope: CoroutineScope): Flow<StopPredictionModel?> {
        return repository.getStopPrediction(_stopId ?: "").flatMapLatest { stopPrediction ->
            if (stopPrediction == null || stopPrediction.predictions.isEmpty()) {
                return@flatMapLatest flowOf(StopPredictionModel(listOf()))
            }
            val stopsDataFormatted =
                stopPrediction.predictions.map { it.routeTag + "|" + it.stopTag }
            repository.requestPredictionsForMultiStops(scope, stopsDataFormatted)
        }
    }

    private fun subscribeToStopStream() {
        viewModelScope.launch(job) {
            stopPredictionStream(viewModelScope).collect { data ->
                data?.let { dataNotNull ->
                    _uiState.update { dataNotNull }
                }
            }
        }
    }
}