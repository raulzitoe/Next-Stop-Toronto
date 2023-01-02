package com.raulvieira.nextstoptoronto.screens.nearme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.raulvieira.nextstoptoronto.Repository
import com.raulvieira.nextstoptoronto.models.StopModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearMeViewModel @Inject constructor(private val repository: Repository): ViewModel() {
    private val _uiState: MutableStateFlow<List<StopModel>> = MutableStateFlow(listOf())
    val uiState: StateFlow<List<StopModel>> = _uiState

    init {
        viewModelScope.launch {
            repository.getStopsFromDatabase().collect { stopsList ->
                _uiState.update { stopsList }
            }
        }
    }
}