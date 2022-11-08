package com.raulvieira.nextstoptoronto.navigation

sealed class Screen (val route: String) {
    object Home: Screen(route = "home_screen")
    object SecondScreen: Screen(route = "second_screen")
}
