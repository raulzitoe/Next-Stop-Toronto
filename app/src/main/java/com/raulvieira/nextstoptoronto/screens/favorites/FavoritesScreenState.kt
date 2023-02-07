package com.raulvieira.nextstoptoronto.screens.favorites

import com.raulvieira.nextstoptoronto.models.StopPredictionModel

sealed class FavoritesScreenState {
    object Loading : FavoritesScreenState()
    data class Success(val data: StopPredictionModel) : FavoritesScreenState()
    object Error : FavoritesScreenState()
}
