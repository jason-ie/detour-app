package com.example.detour.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
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
                onSearchClick = { origin, destination, duration, selectedCities ->
                    // Navigate with basic params in route, pass selectedCities via savedStateHandle
                    navController.currentBackStackEntry?.savedStateHandle?.set("selectedCities", selectedCities)
                    navController.navigate("results/$origin/$destination/$duration")
                }
            )
        }

        composable("results/{origin}/{destination}/{duration}") { backStackEntry ->
            val origin = backStackEntry.arguments?.getString("origin") ?: "Hong Kong (HKG)"
            val destination = backStackEntry.arguments?.getString("destination") ?: "Bali (DPS)"
            val duration = backStackEntry.arguments?.getString("duration")?.toIntOrNull() ?: 7

            // Retrieve selectedCities from savedStateHandle
            val selectedCities = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<List<String>>("selectedCities") ?: emptyList()

            ResultsScreen(
                origin = origin,
                destination = destination,
                tripDuration = duration,
                selectedCities = selectedCities,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}