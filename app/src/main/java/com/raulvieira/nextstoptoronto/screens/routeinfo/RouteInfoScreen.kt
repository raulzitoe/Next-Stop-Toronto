package com.raulvieira.nextstoptoronto.screens.routeinfo


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.raulvieira.nextstoptoronto.components.AnimatedSearchField
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
    var searchedText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var searchVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(searchVisible) {
        if (searchVisible) {
            focusRequester.requestFocus()
        }
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
                },
                actions = {
                    IconButton(onClick = { searchVisible = !searchVisible }) {
                        Icon(Icons.Filled.Search, contentDescription = "Localized description")
                    }
                }
            )
        },
        content = { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                Column {
                    AnimatedSearchField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .focusRequester(focusRequester),
                        searchVisible = searchVisible,
                        searchedText = searchedText,
                        onValueChange = { searchedText = it })
                    StopsLazyColumn(
                        modifier = Modifier.padding(horizontal = 5.dp),
                        stops = uiState.route.stopsList,
                        searchedText = searchedText,
                        onClickStopItem = { stopId -> onClickStop(routeTag, stopId) })
                }
            }
        }
    )
}

@Composable
fun StopsLazyColumn(
    modifier: Modifier = Modifier,
    stops: List<StopModel>,
    searchedText: TextFieldValue,
    onClickStopItem: (String) -> Unit
) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        items(
            items = stops
                .filter {
                    it.stopId.isNotBlank()
                }
                .filter {
                    val words =
                        searchedText.text.split("\\s+".toRegex()).map { word ->
                            word.replace("""^[,.]|[,.]$""".toRegex(), "")
                        }
                    var containsWord = true
                    words.forEach { word ->
                        containsWord = containsWord && it.title.contains(
                            word,
                            ignoreCase = true
                        )
                    }
                    containsWord

                }.sortedBy { it.title }, key = { it.stopId }) { routeInfo ->
            StopInfoCard(
                routeInfo = routeInfo,
                onClick = { stopId -> onClickStopItem(stopId) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopInfoCard(routeInfo: StopModel, onClick: (stopId: String) -> Unit) {
    Card(
        modifier = Modifier
            .wrapContentSize(), onClick = { onClick(routeInfo.stopId) }
    ) {
        Box(
            modifier = Modifier
                .wrapContentHeight().fillMaxWidth().defaultMinSize(minHeight = 48.dp)
        ) {
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

@Preview
@Composable
fun AnimatedSearchFieldPreview() {
    AnimatedSearchField(
        searchVisible = true,
        searchedText = TextFieldValue("Text to Search"),
        onValueChange = {})
}

@Preview
@Composable
fun StopInfoCardPreview() {
    StopInfoCard(
        routeInfo = StopModel("1234", "123", "12.4", "12.4", "Dufferin at Somewhere St "),
        onClick = {})
}

@Preview
@Composable
fun StopsLazyColumnPreview() {
    StopsLazyColumn(
        stops = listOf(
            StopModel(stopId = "1", title = "Stop 1 at that St"),
            StopModel(stopId = "2", title = "Stop 2 at that St"),
            StopModel(stopId = "3", title = "Stop 3 at that St")
        ),
        searchedText = TextFieldValue(""),
        onClickStopItem = {})
}