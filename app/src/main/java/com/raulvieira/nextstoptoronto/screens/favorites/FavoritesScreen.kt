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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.models.FavoritesModel
import com.raulvieira.nextstoptoronto.screens.stopinfo.StopInfoCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

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
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                LazyColumn() {
                    items(uiState.value.predictions) { favoriteItem ->
                        favoriteItem.directions.forEach { direction ->
                            StopInfoCard(
                                predictionInfo = direction,
                                onClick = { },
                                onClickFavorite = { isChecked ->
                                    viewModel.handleFavoriteItem(
                                        isChecked,
                                        FavoritesModel(
                                            id = 0,
                                            routeTag = favoriteItem.routeTag,
                                            stopTag = favoriteItem.stopTag,
                                            stopTitle = favoriteItem.stopTitle
                                        )
                                    )
                                },
                                favoriteButtonChecked = viewModel.isRouteFavorited(
                                    favoriteItem.stopTag,
                                    favoriteItem.routeTag,
                                    favoriteItem.stopTitle
                                ).collectAsStateWithLifecycle(initialValue = false).value
                            )
                        }
                    }
                }
            }
        }
    )

}