package com.raulvieira.nextstoptoronto.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raulvieira.nextstoptoronto.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: Repository): ViewModel() {
    fun getRouteList(){
        viewModelScope.launch {
            Log.e("risos", repository.getRouteList().body().toString())
        }
    }
}