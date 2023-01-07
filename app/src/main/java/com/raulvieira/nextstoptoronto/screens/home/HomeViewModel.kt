package com.raulvieira.nextstoptoronto.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.Repository
import com.raulvieira.nextstoptoronto.models.RouteListModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _uiState: MutableStateFlow<RouteListModel> = MutableStateFlow(
        RouteListModel(
            emptyList()
        )
    )
    val uiState: StateFlow<RouteListModel> = _uiState

    init {
        getRouteList()
    }

    private fun getRouteList() {
        viewModelScope.launch {
            repository.getRouteList().body()?.let { routes ->
                _uiState.update { routes }
            }
        }
    }
}