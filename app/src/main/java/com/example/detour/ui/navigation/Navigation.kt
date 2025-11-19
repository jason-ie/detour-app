package com.example.detour.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.detour.data.MockData
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
                onSearchClick = { originCode, destinationCode, duration, selectedCities ->
                    // Store selectedCities in savedStateHandle - must convert to ArrayList for serialization
                    navController.currentBackStackEntry?.savedStateHandle?.set("selectedCities", ArrayList(selectedCities))

                    // Navigate with clean airport codes
                    navController.navigate("results/$originCode/$destinationCode/$duration")
                }
            )
        }

        composable("results/{origin}/{destination}/{duration}") { backStackEntry ->
            val originCode = backStackEntry.arguments?.getString("origin") ?: "HKG"
            val destinationCode = backStackEntry.arguments?.getString("destination") ?: "DPS"
            val duration = backStackEntry.arguments?.getString("duration")?.toIntOrNull() ?: 7

            // Retrieve selectedCities from savedStateHandle
            val selectedCities = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<List<String>>("selectedCities") ?: emptyList()

            // Find city names from codes
            val originCity = MockData.PopularCities.allCities.find { it.iataCode == originCode }
            val destinationCity = MockData.PopularCities.allCities.find { it.iataCode == destinationCode }

            ResultsScreen(
                origin = originCity?.let { "${it.name} (${it.iataCode})" } ?: originCode,
                destination = destinationCity?.let { "${it.name} (${it.iataCode})" } ?: destinationCode,
                tripDuration = duration,
                selectedCities = selectedCities,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
