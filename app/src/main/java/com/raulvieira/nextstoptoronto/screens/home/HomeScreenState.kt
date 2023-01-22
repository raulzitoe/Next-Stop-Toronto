package com.raulvieira.nextstoptoronto.screens.home

import com.raulvieira.nextstoptoronto.models.RouteListModel

sealed class HomeScreenState {
    object Loading: HomeScreenState()
    data class Success(val data: RouteListModel): HomeScreenState()
    object Error: HomeScreenState()
}
