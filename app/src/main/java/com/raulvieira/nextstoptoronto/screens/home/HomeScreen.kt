package com.raulvieira.nextstoptoronto.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.components.AnimatedSearchField
import com.raulvieira.nextstoptoronto.components.InternetStatusBar
import com.raulvieira.nextstoptoronto.components.ScrollToTopButton
import com.raulvieira.nextstoptoronto.models.RouteLineModel
import isInternetOn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    val showUpdateDialog by viewModel.showUpdateDialog
    val updateProgress by viewModel.updatePercentage
    val scope = rememberCoroutineScope()
    val isInternetOn by isInternetOn(LocalContext.current, scope).collectAsStateWithLifecycle()
    var internetStatusBarVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isInternetOn) {
        internetStatusBarVisible = if (!isInternetOn) {
            true
        } else {
            viewModel.initializeScreenState()
            delay(2000)
            false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.app_name))
                },
                actions = {
                    IconButton(onClick = { searchVisible = !searchVisible }) {
                        Icon(Icons.Filled.Search, contentDescription = "Localized description")
                    }
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
            Column {
                AnimatedVisibility(
                    visible = internetStatusBarVisible,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    InternetStatusBar(isConnected = isInternetOn)
                }
                when (val state = uiState) {
                    is HomeScreenState.Loading -> HomeScreenLoading()
                    is HomeScreenState.Success -> {
                        val routeList = state.data.routeList
                        HomeScreenSuccess(
                            routeList = routeList,
                            searchVisible = searchVisible,
                            onNavigate = { onNavigate(it) },
                            showUpdateDialog = showUpdateDialog,
                            updateProgress = updateProgress
                        )
                    }

                    is HomeScreenState.Error -> HomeScreenError()
                }
            }
        }
    }
}

@Composable
private fun HomeScreenSuccess(
    routeList: List<RouteLineModel>,
    searchVisible: Boolean,
    onNavigate: (String) -> Unit,
    showUpdateDialog: Boolean,
    updateProgress: Float
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

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("Updating Stops") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(
                        modifier = Modifier.padding(vertical = 5.dp),
                        text = (updateProgress * 100).toInt().toString() + "%"
                    )
                    Text(
                        modifier = Modifier.padding(vertical = 5.dp),
                        text = "Downloading updated stop locations from the TTC to show on your map, " +
                                "this happens every few months"
                    )
                }
            }
        )
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
            onClear = { searchedText = TextFieldValue("") }
        )
        RouteGrid(
            modifier = Modifier.padding(horizontal = 5.dp),
            routeList = routeList,
            searchedText = searchedText,
            onClickRoute = { onNavigate(it) })
    }
}

@Composable
private fun HomeScreenLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun HomeScreenError() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ERROR")
    }
}

@Composable
private fun RouteGrid(
    modifier: Modifier = Modifier,
    routeList: List<RouteLineModel>,
    searchedText: TextFieldValue,
    onClickRoute: (String) -> Unit
) {
    val listState = rememberLazyGridState()
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
        LazyVerticalGrid(
            modifier = modifier,
            columns = GridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            state = listState
        ) {
            items(items = routeList.filter {
                val words = searchedText.text.split("\\s+".toRegex()).map { word ->
                    word.replace("""^[,.]|[,.]$""".toRegex(), "")
                }
                var containsWord = true
                words.forEach { word ->
                    containsWord =
                        containsWord && it.title.contains(word, ignoreCase = true)
                }
                containsWord

            },
                key = { it.routeTag }
            ) { route ->
                RouteCard(
                    route = route,
                    onClick = { onClickRoute(route.routeTag) })
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
private fun RouteCard(modifier: Modifier = Modifier, route: RouteLineModel, onClick: () -> Unit) {
    Card(modifier = modifier.wrapContentSize(), onClick = onClick) {
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            Text(
                text = route.title,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(10.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun HomeScreenSuccessPreview() {
    HomeScreenSuccess(
        routeList = listOf(
            RouteLineModel(routeTag = "41", title = "41-Keele"),
            RouteLineModel(routeTag = "42", title = "42-Keele"),
            RouteLineModel(routeTag = "43", title = "43-Keele"),
            RouteLineModel(routeTag = "44", title = "44-Keele")
        ),
        searchVisible = true,
        onNavigate = {},
        showUpdateDialog = false,
        updateProgress = 0.3f
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    HomeScreenLoading()
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun HomeScreenErrorPreview() {
    HomeScreenError()
}