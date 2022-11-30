package com.raulvieira.nextstoptoronto.screens.routeinfo


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.raulvieira.nextstoptoronto.models.StopModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun RouteInfoScreen(
    viewModel: RouteInfoViewModel = hiltViewModel(),
    routeTag: String = "N/A",
    onNavigateUp: () -> Unit,
    onClickStop: (routeTag: String, stopId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var searchText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

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
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Search") }
                    )
                    LazyColumn() {
                        items(
                            items = uiState.route.stopsList
                                .filter {
                                   !it.stopId.isNullOrBlank()
                                }
                                .filter {
                                val words = searchText.text.split("\\s+".toRegex()).map { word ->
                                    word.replace("""^[,\.]|[,\.]$""".toRegex(), "")
                                }
                                var containsWord: Boolean = true
                                words.forEach { word->
                                   containsWord = containsWord && it.title.contains(word, ignoreCase = true)
                                }
                                containsWord

                            }.sortedBy { it.title }, key = { it.stopId }) { routeInfo ->
                            RouteInfoCard(
                                routeInfo = routeInfo,
                                onClick = { stopId -> onClickStop(routeTag, stopId) })
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteInfoCard(routeInfo: StopModel, onClick: (stopId: String) -> Unit) {
    Card(
        modifier = Modifier
            .height(60.dp)
            .padding(10.dp), onClick = { onClick(routeInfo.stopId) }
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