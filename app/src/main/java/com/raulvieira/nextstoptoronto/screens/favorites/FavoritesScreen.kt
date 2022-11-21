package com.raulvieira.nextstoptoronto.screens.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.screens.stopinfo.StopInfoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.favorites))
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigateUp() }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Localized description")
                    }
                }
            )
        },
        content = { innerPadding ->
            Box(modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()) {
                LazyColumn() {
                    items(uiState.value.predictions) { favoriteItem ->
                        favoriteItem.directions.forEach { direction ->
                            StopInfoCard(
                                predictionInfo = direction,
                                onClick = { },
                                onClickFavorite = { },
                                favoriteButtonChecked = false
                            )
                        }
                    }
                }
            }
        }
    )

}