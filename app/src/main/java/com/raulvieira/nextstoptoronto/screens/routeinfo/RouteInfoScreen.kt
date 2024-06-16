package com.raulvieira.nextstoptoronto.screens.routeinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulvieira.nextstoptoronto.components.AnimatedSearchField
import com.raulvieira.nextstoptoronto.components.InternetStatusBar
import com.raulvieira.nextstoptoronto.components.ScrollToTopButton
import com.raulvieira.nextstoptoronto.models.RouteConfigurationModel
import com.raulvieira.nextstoptoronto.models.RouteModel
import com.raulvieira.nextstoptoronto.models.StopModel
import isInternetOn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteInfoScreen(
    viewModel: RouteInfoViewModel = hiltViewModel(),
    routeTag: String = "",
    onNavigateUp: () -> Unit,
    onClickStop: (routeTag: String, stopId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isInternetOn by isInternetOn(LocalContext.current, scope).collectAsStateWithLifecycle()
    var internetStatusBarVisible by remember { mutableStateOf(false) }


    LaunchedEffect(key1 = routeTag) {
        viewModel.initializeScreenState(routeTag = routeTag)
    }

    LaunchedEffect(isInternetOn) {
        internetStatusBarVisible = if (!isInternetOn) {
            true
        } else {
            viewModel.initializeScreenState(routeTag = routeTag)
            delay(2000)
            false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is RouteInfoScreenState.Success -> Text(state.data.route.title)
                        else -> Text("N/A")
                    }
                },
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
        }
    )
    { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column {
                AnimatedVisibility(
                    visible = internetStatusBarVisible,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    InternetStatusBar(isConnected = isInternetOn)
                }
                when (val state = uiState) {
                    is RouteInfoScreenState.Loading -> RouteInfoScreenLoading()
                    is RouteInfoScreenState.Success -> {
                        val routeConfiguration = state.data
                        RouteInfoScreenSuccess(
                            routeConfiguration = routeConfiguration,
                            onClickStop = { stopId -> onClickStop(routeTag, stopId) },
                            searchVisible = searchVisible
                        )
                    }

                    is RouteInfoScreenState.Error -> RouteInfoScreenError()
                }
            }
        }
    }
}

@Composable
private fun RouteInfoScreenSuccess(
    routeConfiguration: RouteConfigurationModel,
    onClickStop: (String) -> Unit,
    searchVisible: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    var searchedText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    LaunchedEffect(searchVisible) {
        if (searchVisible) {
            focusRequester.requestFocus()
        }
    }

    Column {
        AnimatedSearchField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .focusRequester(focusRequester),
            searchVisible = searchVisible,
            searchedText = searchedText,
            onValueChange = { searchedText = it },
            onClear = { searchedText = TextFieldValue("") })
        StopsLazyColumn(
            modifier = Modifier.padding(horizontal = 5.dp),
            stops = routeConfiguration.route.stopsList,
            searchedText = searchedText,
            onClickStopItem = { stopId -> onClickStop(stopId) })
    }
}

@Composable
private fun RouteInfoScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun RouteInfoScreenError() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ERROR")
    }
}

@Composable
private fun StopsLazyColumn(
    modifier: Modifier = Modifier,
    stops: List<StopModel>,
    searchedText: TextFieldValue,
    onClickStopItem: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showScrollButton by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect {
            if (it > 0) {
                showScrollButton = true
                delay(800)
                showScrollButton = false
            }
        }
    }

    Box {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            state = listState
        ) {
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

                    }.sortedBy { it.title },
            ) { routeInfo ->
                StopInfoCard(
                    routeInfo = routeInfo,
                    onClick = { stopId -> onClickStopItem(stopId) })
            }
        }
        ScrollToTopButton(
            onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
            showButton = showScrollButton
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StopInfoCard(routeInfo: StopModel, onClick: (stopId: String) -> Unit) {
    Card(
        modifier = Modifier
            .wrapContentSize(), onClick = { onClick(routeInfo.stopId) }
    ) {
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun RouteInfoScreenSuccessPreview() {
    RouteInfoScreenSuccess(
        routeConfiguration = RouteConfigurationModel(
            route = RouteModel(
                title = "29-Dufferin",
                stopsList = listOf(
                    StopModel(title = "This road at that road", stopId = "1"),
                    StopModel(title = "This road at that road", stopId = "2"),
                    StopModel(title = "This road at that road", stopId = "3")
                ),
                paths = listOf()
            )
        ),
        onClickStop = {},
        searchVisible = true
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun RouteInfoScreenLoadingPreview() {
    RouteInfoScreenLoading()
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun RouteInfoScreenErrorPreview() {
    RouteInfoScreenError()
}