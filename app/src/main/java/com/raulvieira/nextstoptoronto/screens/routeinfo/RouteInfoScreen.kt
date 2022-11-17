package com.raulvieira.nextstoptoronto.screens.routeinfo


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    routeTag: String = "N/A",
    onNavigateUp: () -> Unit,
    onClickStop: (routeTag:String, stopTag:String) -> Unit
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
                title = { Text(uiState.route.title) },
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
            Surface(modifier = Modifier.padding(innerPadding)) {
                Column {
                    LazyColumn() {
                        items(uiState.route.stopsList) { routeInfo ->
                            RouteInfoCard(
                                routeInfo = routeInfo,
                                onClick = { stopTag -> onClickStop(routeTag, stopTag) })
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteInfoCard(routeInfo: StopsModel, onClick: (stopTag:String) -> Unit) {
    Card(
        modifier = Modifier
            .height(60.dp)
            .padding(10.dp), onClick = { onClick(routeInfo.tag) }
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