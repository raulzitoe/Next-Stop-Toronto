package com.raulvieira.nextstoptoronto.screens.favorites

import androidx.lifecycle.ViewModel
import com.raulvieira.nextstoptoronto.database.RoomDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(val database: RoomDao) : ViewModel() {

}