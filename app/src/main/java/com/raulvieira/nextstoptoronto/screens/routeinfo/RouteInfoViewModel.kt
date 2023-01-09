package com.raulvieira.nextstoptoronto.screens.routeinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.database.Repository
import com.raulvieira.nextstoptoronto.models.RouteModel
import com.raulvieira.nextstoptoronto.models.RouteConfigModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteInfoViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _uiState: MutableStateFlow<RouteConfigModel> = MutableStateFlow(
        RouteConfigModel(
            RouteModel(
                "",
                listOf()
            )
        )
    )
    val uiState: StateFlow<RouteConfigModel> = _uiState

    fun getRouteConfig(routeTag: String) {
        if (routeTag.isNotEmpty()) {
            viewModelScope.launch {
                repository.getRouteConfig(routeTag).collect { data ->
                    if (data == null) return@collect
                    _uiState.update { data }
                }
            }
        }
    }
}