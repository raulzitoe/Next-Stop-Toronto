package com.raulvieira.nextstoptoronto.screens.routeinfo


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.raulvieira.nextstoptoronto.models.StopsModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RouteInfoScreen(
    viewModel: RouteInfoViewModel = hiltViewModel(),
    routeTag: String = "N/A"
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(key1 = Unit) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            launch {
                viewModel.getRouteConfig(routeTag)
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route $routeTag") },
                navigationIcon = {
                    IconButton(
                        onClick = { /* "Open nav drawer" */ }
                    ) {
                        Icon(Icons.Filled.Menu, contentDescription = "Localized description")
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* fab click handler */ }
            ) {
                Text("Inc")
            }
        },
        content = { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                Column {
                    Text(uiState.route.title)
                    LazyColumn() {
                        items(uiState.route.stop) { routeInfo ->
                            RouteInfoCard(routeInfo = routeInfo)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun RouteInfoCard(routeInfo: StopsModel) {
    Card(
        modifier = Modifier
            .height(60.dp)
            .padding(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = routeInfo.title, textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(5.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}