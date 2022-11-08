package com.raulvieira.nextstoptoronto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raulvieira.nextstoptoronto.screens.home.HomeScreen
import com.raulvieira.nextstoptoronto.screens.second.SecondScreen

@Composable
fun MyAppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    // Navigation should happen here with a callback from screen
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Home.route) { HomeScreen(onNavigate = { screen ->  navController.navigate(screen.route) }) }
        composable(route = Screen.SecondScreen.route) { SecondScreen(onNavigate = { screen -> navController.navigate(screen.route) }) }
    }
}