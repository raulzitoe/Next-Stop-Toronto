package com.raulvieira.nextstoptoronto.screens.routeinfo

import com.raulvieira.nextstoptoronto.models.RouteConfigurationModel

sealed class RouteInfoScreenState {
    object Loading: RouteInfoScreenState()
    data class Success(val data: RouteConfigurationModel): RouteInfoScreenState()
    object Error: RouteInfoScreenState()
}