package com.raulvieira.nextstoptoronto.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.models.RouteLineModel

@OptIn(ExperimentalLifecycleComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit

) {
    val routes by viewModel.uiState.collectAsStateWithLifecycle()
    var searchText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var searchVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

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
        },
        content = { innerPadding ->
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    AnimatedVisibility(visible = searchVisible) {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .focusRequester(focusRequester),
                            value = searchText,
                            onValueChange = { searchText = it },
                            label = { Text("Search") }
                        )
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp)
                    ) {
                        items(items = routes.routeList.filter {
                            val words = searchText.text.split("\\s+".toRegex()).map { word ->
                                word.replace("""^[,\.]|[,\.]$""".toRegex(), "")
                            }
                            var containsWord: Boolean = true
                            words.forEach { word ->
                                containsWord =
                                    containsWord && it.title.contains(word, ignoreCase = true)
                            }
                            containsWord

                        },
                            key = { it.routeTag }
                        ) { route ->
                            RouteCard(
                                modifier = Modifier.height(60.dp),
                                route = route,
                                onClick = { onNavigate(route.routeTag) })
                        }
                    }
                }
            }
        })

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