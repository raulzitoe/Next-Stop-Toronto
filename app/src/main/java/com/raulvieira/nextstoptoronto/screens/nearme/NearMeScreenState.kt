package com.raulvieira.nextstoptoronto.screens.nearme

import com.raulvieira.nextstoptoronto.models.StopPredictionModel


sealed class NearMeScreenState {
    object Loading : NearMeScreenState()
    data class Success(val data: StopPredictionModel) : NearMeScreenState()
    object Error : NearMeScreenState()
}