package com.raulvieira.nextstoptoronto.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NearMe
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.raulvieira.nextstoptoronto.R
import com.raulvieira.nextstoptoronto.screens.favorites.FavoritesScreen
import com.raulvieira.nextstoptoronto.screens.home.HomeScreen
import com.raulvieira.nextstoptoronto.screens.map.MapScreen
import com.raulvieira.nextstoptoronto.screens.nearme.NearMeScreen
import com.raulvieira.nextstoptoronto.screens.routeinfo.RouteInfoScreen
import com.raulvieira.nextstoptoronto.screens.stopinfo.StopInfoScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    val bottomBarItems = listOf(
        listOf(Screen.Home, Icons.Filled.Home, stringResource(id = R.string.home)),
        listOf(Screen.MapScreen, Icons.Filled.Public, stringResource(id = R.string.map)),
        listOf(Screen.NearMeScreen, Icons.Filled.NearMe, stringResource(id = R.string.near_me)),
        listOf(
            Screen.FavoritesScreen,
            Icons.Filled.Favorite,
            stringResource(id = R.string.favorites)
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomBarItems.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                (item.component2() as ImageVector),
                                contentDescription = null
                            )
                        },
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
            startDestination = startDestination,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onNavigate = { busRouteTag ->
                    navController.navigate(
                        "${Screen.RouteInfoScreen.route}?routeTag=${busRouteTag}"
                    )
                })
            }
            composable(Screen.MapScreen.route) { MapScreen() }
            composable(Screen.FavoritesScreen.route) { FavoritesScreen() }
            composable(
                route = "${Screen.RouteInfoScreen.route}?routeTag={routeTag}",
                arguments = listOf(navArgument("routeTag") { type = NavType.StringType })
            ) {
                RouteInfoScreen(
                    routeTag = it.arguments?.getString("routeTag") ?: " ",
                    onNavigateUp = { navController.navigateUp() },
                    onClickStop = { routeTag, stopId ->
                        navController.navigate(
                            "${Screen.StopInfoScreen.route}?routeTag=${routeTag}&stopId=${stopId}"
                        )
                    })
            }
            composable(
                route = "${Screen.StopInfoScreen.route}?routeTag={routeTag}&stopId={stopId}",
                arguments = listOf(
                    navArgument("routeTag") { type = NavType.StringType },
                    navArgument("stopId") { type = NavType.StringType })
            ) {
                StopInfoScreen(
                    routeTag = it.arguments?.getString("routeTag") ?: " ",
                    onNavigateUp = { navController.navigateUp() })
            }
            composable(route = Screen.NearMeScreen.route) { NearMeScreen()}
        }
    }
}
