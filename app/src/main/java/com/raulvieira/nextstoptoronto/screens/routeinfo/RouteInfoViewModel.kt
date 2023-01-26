package com.raulvieira.nextstoptoronto.screens.routeinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteInfoViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _uiState: MutableStateFlow<RouteInfoScreenState> = MutableStateFlow(
        RouteInfoScreenState.Loading
    )
    val uiState: StateFlow<RouteInfoScreenState> = _uiState

    fun initializeScreenState(routeTag: String) {
        if (routeTag.isNotEmpty() && (_uiState.value is RouteInfoScreenState.Loading
                    || _uiState.value is RouteInfoScreenState.Error)
        ) {
            viewModelScope.launch {
                repository.getRouteConfig(routeTag).collect { data ->
                    data?.let { dataNotNull ->
                        _uiState.update { RouteInfoScreenState.Success(data = dataNotNull) }
                    }
                }
            }
        }
    }
}