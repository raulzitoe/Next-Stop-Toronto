package com.raulvieira.nextstoptoronto.screens.favorites


import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
class FavoritesViewModel @Inject constructor(val repository: Repository) : ViewModel(),
    DefaultLifecycleObserver {

    private val favoritesPrediction: MutableStateFlow<StopPredictionModel> = MutableStateFlow(
        StopPredictionModel(listOf())
    )
    private val isFavoriteEmpty: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val uiState: StateFlow<FavoritesScreenState> =
        combine(isFavoriteEmpty, favoritesPrediction) { isFavoriteEmpty, favoritesPrediction ->
            if(isFavoriteEmpty){
                FavoritesScreenState.Success(data = StopPredictionModel(listOf()))
            }
            else {
                FavoritesScreenState.Success(data = favoritesPrediction)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), FavoritesScreenState.Loading)



    private var job = Job()
        get() {
            if (field.isCancelled) field = Job()
            return field
        }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        subscribeToFavorites()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun stopPredictionStream(scope: CoroutineScope): Flow<StopPredictionModel?> {
        return repository.getFavorites().flatMapLatest { fav ->
            val stopsDataFormatted = fav.map { it.routeTag + "|" + it.stopTag }
            repository.requestPredictionsForMultiStops(scope, stopsDataFormatted)
        }
    }

    fun subscribeToFavorites() {
        viewModelScope.launch {
            repository.isFavoritesEmpty().collect { isEmpty ->
                isFavoriteEmpty.update { isEmpty }
            }
        }
        viewModelScope.launch(job) {
            stopPredictionStream(viewModelScope).collect { data ->
                data?.let { dataNotNull ->
                    favoritesPrediction.update { dataNotNull }
                }
            }
        }
    }

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