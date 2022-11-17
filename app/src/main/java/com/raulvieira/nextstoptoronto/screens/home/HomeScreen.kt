package com.raulvieira.nextstoptoronto.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raulvieira.nextstoptoronto.models.RouteLineModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit

) {
    val routes by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp)
        ) {
            items(routes.routeList) { route ->
                RouteCard(modifier = Modifier.height(60.dp), route = route, onClick = { onNavigate(route.routeTag) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteCard(modifier: Modifier, route: RouteLineModel, onClick: () -> Unit) {
    Card(modifier = modifier.padding(5.dp), onClick = onClick) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = route.title,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(5.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BusCardPreview() {
    RouteCard(modifier = Modifier, RouteLineModel("41", "41 - Keele"), onClick = {})
}