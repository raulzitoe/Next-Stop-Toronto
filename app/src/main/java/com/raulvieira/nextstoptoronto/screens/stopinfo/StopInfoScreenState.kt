package com.raulvieira.nextstoptoronto.screens.stopinfo

import com.raulvieira.nextstoptoronto.models.StopPredictionModel

sealed class StopInfoScreenState {
    object Loading: StopInfoScreenState()
    data class Success(val data: StopPredictionModel): StopInfoScreenState()
    object Error: StopInfoScreenState()
}
