package com.raulvieira.nextstoptoronto.screens.stopinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.raulvieira.nextstoptoronto.models.PredictionModel
import com.raulvieira.nextstoptoronto.models.SinglePredictionModel
import com.raulvieira.nextstoptoronto.models.StopsModel
import com.raulvieira.nextstoptoronto.screens.routeinfo.RouteInfoCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopInfoScreen(
    viewModel: StopInfoViewModel = hiltViewModel(),
    routeTag: String,
    stopTag: String,
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(key1 = Unit) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            launch {
                viewModel.getStopPrediction(routeTag = routeTag, stopTag = stopTag)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.predictions.isNotEmpty()) {
                            uiState.predictions.first().stopTitle
                        } else {
                            "N/A"
                        }
                    )
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
            Surface(modifier = Modifier.padding(innerPadding)) {
                Column {
                    LazyColumn() {
                        items(
                            if (uiState.predictions.isNotEmpty()) {
                                uiState.predictions.first().direction.first().prediction
                            } else {
                                arrayListOf()
                            }
                        ) { routeInfo ->
                            StopInfoCard(
                                stopInfo = routeInfo,
                                onClick = { })
                        }
                    }
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopInfoCard(stopInfo: SinglePredictionModel, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .height(60.dp)
            .padding(10.dp), onClick = { }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stopInfo.minutes, textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(5.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}