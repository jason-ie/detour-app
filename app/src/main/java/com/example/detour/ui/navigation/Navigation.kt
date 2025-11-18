package com.example.detour.ui.navigation

import android.util.Log
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
                    Log.d("Navigation", "onSearchClick received! Origin: $originCode, Dest: $destinationCode, Duration: $duration")
                    // Store selectedCities in savedStateHandle
                    navController.currentBackStackEntry?.savedStateHandle?.set("selectedCities", selectedCities)

                    val route = "results/$originCode/$destinationCode/$duration"
                    Log.d("Navigation", "Navigating to: $route")
                    // Navigate with clean airport codes
                    navController.navigate(route)
                    Log.d("Navigation", "Navigation call completed")
                }
            )
        }

        composable("results/{origin}/{destination}/{duration}") { backStackEntry ->
            Log.d("Navigation", "Results composable invoked!")
            val originCode = backStackEntry.arguments?.getString("origin") ?: "HKG"
            val destinationCode = backStackEntry.arguments?.getString("destination") ?: "DPS"
            val duration = backStackEntry.arguments?.getString("duration")?.toIntOrNull() ?: 7
            Log.d("Navigation", "Results route params - Origin: $originCode, Dest: $destinationCode, Duration: $duration")

            // Retrieve selectedCities from savedStateHandle
            val selectedCities = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<List<String>>("selectedCities") ?: emptyList()

            // Find city names from codes
            val originCity = MockData.PopularCities.allCities.find { it.iataCode == originCode }
            val destinationCity = MockData.PopularCities.allCities.find { it.iataCode == destinationCode }

            Log.d("Navigation", "Rendering ResultsScreen")
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
