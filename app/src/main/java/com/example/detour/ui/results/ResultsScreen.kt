package com.example.detour.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.detour.data.FlightApi
import android.util.Log



fun extractIata(text: String): String {
    // if string contains "(HKG)" → returns "HKG"
    val regex = "\\(([A-Z]{3})\\)".toRegex()
    val match = regex.find(text)
    if (match != null) return match.groupValues[1]

    // if already "HKG"
    if (text.length == 3 && text.all { it.isUpperCase() }) return text

    throw IllegalArgumentException("Invalid input: $text")
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    origin: String,
    destination: String,
    tripDuration: Int,
    selectedCities: List<String>,
    startDate: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    var offers by remember { mutableStateOf<List<Map<String, String>>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Call the real API when this screen is shown
    LaunchedEffect(origin, destination, startDate, selectedCities) {
        isLoading = true
        errorMessage = null
        val originCode = extractIata(origin)
        val destinationCode = extractIata(destination)

        Log.d("ResultsScreen", "Searching flights for $originCode -> $destinationCode on $startDate")

        FlightApi.searchFlights(
            context = context,
            origin = originCode,
            destination = destinationCode,
            departureDate = startDate,
            adults = 1,
            onSuccess = { result ->
                offers = result
                isLoading = false
            },
            onError = { e ->
                e.printStackTrace()  // logs full error
                errorMessage = e.toString()
                isLoading = false
            }
        )
    }
    val selectedLayoverCodes = remember(selectedCities) {
        selectedCities.mapNotNull { city ->
            runCatching { extractIata(city) }.getOrNull()
        }
    }
    val primaryLayoverCode = selectedLayoverCodes.firstOrNull()


    // Compute direct + detour + savings once we have offers
    val directOffer = remember(offers) {
        offers
            ?.sortedBy { it["stops"]?.toIntOrNull() ?: 0 }
            ?.firstOrNull()
    }

    val detourOffer = remember(offers, directOffer, primaryLayoverCode) {
        // All flights with at least 1 stop
        val allDetours = offers
            ?.filter { (it["stops"]?.toIntOrNull() ?: 0) > 0 }
            ?: emptyList()

        if (allDetours.isEmpty()) {
            // No layover flights exist at all → show none
            null
        } else if (primaryLayoverCode == null) {
            // No specific layover chosen → show cheapest detour
            allDetours.minByOrNull { it["price"]?.toDoubleOrNull() ?: Double.MAX_VALUE }
        } else {
            // Try to match flights that go through the chosen layover city
            val matchingVia = allDetours.filter { offer ->
                val route = offer["fullRoute"].orEmpty()
                route.contains(primaryLayoverCode, ignoreCase = false)
            }

            // If any match the selected layover, use those; otherwise, fall back to all detours
            val candidatePool = if (matchingVia.isNotEmpty()) matchingVia else allDetours

            candidatePool.minByOrNull { it["price"]?.toDoubleOrNull() ?: Double.MAX_VALUE }
        }
    }


    val savings: Int = remember(directOffer, detourOffer) {
        val directPrice = directOffer?.get("price")?.toDoubleOrNull()
        val detourPrice = detourOffer?.get("price")?.toDoubleOrNull()
        if (directPrice != null && detourPrice != null && directPrice > detourPrice) {
            (directPrice - detourPrice).toInt()
        } else {
            0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flight Options") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Round-trip from $origin to $destination ($tripDuration days${
                    if (selectedCities.isNotEmpty())
                        ", ${selectedCities.size} cities selected"
                    else ""
                })",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Text(
                        text = "Error loading flights: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                offers.isNullOrEmpty() -> {
                    Text(
                        text = "No flight offers found.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
                    // Savings card (only meaningful if we have both offers)
                    if (directOffer != null && detourOffer != null && savings > 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "💰 YOU SAVE",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$savings HKD",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "by choosing the multi-stop route!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // Direct flight card
                    directOffer?.let {
                        FlightOfferCard(
                            offer = it,
                            isRecommended = false
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Detour card
                    detourOffer?.let { detour ->
                        val detourPrice = detour["price"]?.toDoubleOrNull() ?: Double.MAX_VALUE
                        val directPrice = directOffer?.get("price")?.toDoubleOrNull() ?: Double.MAX_VALUE

                        val shouldRecommend = detourPrice < directPrice

                        FlightOfferCard(
                            offer = detour,
                            isRecommended = shouldRecommend
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlightOfferCard(
    offer: Map<String, String>,
    isRecommended: Boolean
) {
    val stops = offer["stops"]?.toIntOrNull() ?: 0
    val price = offer["price"]?.toDoubleOrNull()?.toInt() ?: 0
    val origin = offer["origin"] ?: ""
    val destination = offer["destination"] ?: ""
    val duration = offer["duration"] ?: ""
    val departureTime = offer["departureTime"] ?: ""
    val arrivalTime = offer["arrivalTime"] ?: ""
    val fullRoute = offer["fullRoute"].orEmpty()
    val airline = offer["airline"].orEmpty()



    val flightTypeName = if (stops == 0) "Direct Flight" else "$stops-stop Detour"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = flightTypeName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (isRecommended) {
                        Text(
                            text = "⭐ RECOMMENDED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF9800),
                            modifier = Modifier
                                .background(
                                    Color(0xFFFF9800).copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "$price HKD",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecommended) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (fullRoute.isNotEmpty()) fullRoute else "$origin → $destination",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (airline.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = airline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }



            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Depart: $departureTime\nArrive: $arrivalTime",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (stops == 0) "Non-stop" else "$stops stops",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = duration,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
