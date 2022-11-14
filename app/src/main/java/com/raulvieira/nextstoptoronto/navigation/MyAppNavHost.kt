package com.raulvieira.nextstoptoronto.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.screens.home.HomeScreen
import com.raulvieira.nextstoptoronto.screens.second.SecondScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    val items = listOf(
        listOf(Screen.Home, Icons.Filled.Home, stringResource(id = R.string.home)),
        listOf(Screen.MapScreen, Icons.Filled.Public, stringResource(id = R.string.map))
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon((item.component2() as ImageVector), contentDescription = null) },
                        label = { Text(item.component3() as String) },
                        selected = currentDestination?.hierarchy?.any { it.route == (item.component1() as Screen).route } == true,
                        onClick = {
                            navController.navigate((item.component1() as Screen).route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Home.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(onNavigate = {}) }
            composable(Screen.MapScreen.route) { SecondScreen(onNavigate = {}) }
        }
    }
}
