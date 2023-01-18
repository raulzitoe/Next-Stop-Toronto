package com.raulvieira.nextstoptoronto.screens.home

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
import com.raulvieira.nextstoptoronto.components.ScrollToTopButton
import com.raulvieira.nextstoptoronto.models.RouteLineModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit

) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchedText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val showDialog by viewModel.showDialog
    val updateProgress by viewModel.updatePercentage

    LaunchedEffect(searchVisible) {
        if (searchVisible) {
            focusRequester.requestFocus()
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
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {},
                    confirmButton = {},
                    title = { Text("Updating Stops") },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text(modifier = Modifier.padding(vertical = 5.dp), text = (updateProgress * 100).toInt().toString() + "%")
                            Text(modifier = Modifier.padding(vertical = 5.dp),
                                text = "Downloading updated stop locations from the TTC to show on your map, " +
                                        "this happens every few months"
                            )
                        }
                    }
                )
            }
            if (uiState.routeList.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column {
                    AnimatedSearchField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .focusRequester(focusRequester),
                        searchVisible = searchVisible,
                        searchedText = searchedText,
                        onValueChange = { searchedText = it },
                        onClear = { searchedText = TextFieldValue("") }
                    )
                    RouteGrid(
                        modifier = Modifier.padding(horizontal = 5.dp),
                        routeList = uiState.routeList,
                        searchedText = searchedText,
                        onClickRoute = { onNavigate(it) })
                }
            }

        }
    }
}

@Composable
fun RouteGrid(
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
fun RouteCard(modifier: Modifier = Modifier, route: RouteLineModel, onClick: () -> Unit) {
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

@Preview
@Composable
fun AnimatedSearchFieldPreview() {
    AnimatedSearchField(
        searchVisible = true,
        searchedText = TextFieldValue("Text to Search"),
        onValueChange = {},
        onClear = {})
}

@Preview(showBackground = true)
@Composable
fun RouteCardPreview() {
    RouteCard(route = RouteLineModel("41", "41 - Keele"), onClick = {})
}

@Preview
@Composable
fun RouteGridPreview() {
    RouteGrid(
        routeList =
        listOf(
            RouteLineModel(routeTag = "41", title = "41-Keele"),
            RouteLineModel(routeTag = "42", title = "42-Keele"),
            RouteLineModel(routeTag = "43", title = "43-Keele"),
            RouteLineModel(routeTag = "44", title = "44-Keele")
        ),
        searchedText = TextFieldValue(""),
        onClickRoute = {})
}