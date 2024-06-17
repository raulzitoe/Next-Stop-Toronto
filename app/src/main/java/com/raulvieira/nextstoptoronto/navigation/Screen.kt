package com.raulvieira.nextstoptoronto.navigation

sealed class Screen (val route: String) {
    data object Home: Screen(route = "home_screen")
    data object MapScreen: Screen(route = "map_screen")
    data object FavoritesScreen: Screen(route = "favorites_screen")
    data object RouteInfoScreen: Screen(route = "route_info_screen")
    data object StopInfoScreen: Screen(route = "stop_info_screen")
    data object NearMeScreen: Screen(route = "near_me_screen")
}
