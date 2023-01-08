package com.raulvieira.nextstoptoronto.screens.favorites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.components.StopsLazyColumn
import com.raulvieira.nextstoptoronto.models.FavoritesModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            StopsLazyColumn(
                predictions = uiState.predictions,
                onClickFavoriteItem = { isChecked, favoriteItem ->
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
                favoriteButtonChecked = { routeToCheck ->
                    viewModel.isRouteFavorited(
                        routeToCheck.stopTag,
                        routeToCheck.routeTag,
                        routeToCheck.stopTitle
                    ).collectAsStateWithLifecycle(initialValue = false).value
                },
                distanceToStop = { "" })
        }
    }
}