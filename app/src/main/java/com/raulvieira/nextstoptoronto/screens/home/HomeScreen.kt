package com.raulvieira.nextstoptoronto.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raulvieira.nextstoptoronto.navigation.Screen

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (Screen) -> Unit
) {
    val routes by viewModel.uiState.collectAsState()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
            LazyColumn(Modifier.fillMaxSize()){
                items(routes.routeList) { item ->
                    Row() {
                        Text(text = item.tag, color = Color.Black)
                        Spacer(modifier = Modifier.size(5.dp))
                        Text(text = item.title)
                    }
                }
            }
    }
}