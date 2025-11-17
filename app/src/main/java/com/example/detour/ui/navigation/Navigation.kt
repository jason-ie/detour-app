package com.example.detour.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.detour.ui.results.ResultsScreen
import com.example.detour.ui.search.SearchScreen

@Composable
fun DetourNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "search"
    ) {
        composable("search") {
            SearchScreen(
                onSearchClick = { origin, destination ->
                    navController.navigate("results")
                }
            )
        }

        composable("results") {
            ResultsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}