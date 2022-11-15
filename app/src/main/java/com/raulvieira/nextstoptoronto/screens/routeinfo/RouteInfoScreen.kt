package com.raulvieira.nextstoptoronto.screens.routeinfo


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raulvieira.nextstoptoronto.RouteModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RouteInfoScreen(routeTag: String = "N/A") {
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
            Box(modifier = Modifier.consumedWindowInsets(innerPadding)) {
                Text(text = "route screen")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun RouteScreenPreview() {
    RouteInfoScreen("41")
}