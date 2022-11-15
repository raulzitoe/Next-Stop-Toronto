package com.raulvieira.nextstoptoronto.navigation

sealed class Screen (val route: String) {
    object Home: Screen(route = "home_screen")
    object MapScreen: Screen(route = "map_screen")
    object FavoritesScreen: Screen(route = "favorites_screen")
}
