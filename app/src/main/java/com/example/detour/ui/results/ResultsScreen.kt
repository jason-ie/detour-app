package com.example.detour.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.detour.data.MockData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    origin: String,
    destination: String,
    tripDuration: Int,
    selectedCities: List<String>,
    onBackClick: () -> Unit
) {
    val allOffers = MockData.getAllFlightOffers()
    val directFlight = allOffers[0]  // Direct flight
    val bestDetourOffer = allOffers[2]  // Multi-city detour route with best savings
    val savings = MockData.calculateSavings(bestDetourOffer)

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
            // Round-trip context display
            Text(
                text = "Round-trip from $origin to $destination ($tripDuration days${if (selectedCities.isNotEmpty()) ", ${selectedCities.size} cities selected" else ""})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Savings Card (Most Prominent)
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
                        text = "${savings.toInt()} HKD",
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

            // Direct Flight Option
            FlightOfferCard(
                offer = directFlight,
                isRecommended = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Multi-Stop Detour Option
            FlightOfferCard(
                offer = bestDetourOffer,
                isRecommended = true
            )
        }
    }
}

@Composable
fun FlightOfferCard(
    offer: MockData.FlightOffer,
    isRecommended: Boolean
) {
    val itinerary = offer.itineraries.first()
    val segments = itinerary.segments

    // Build route description (e.g., "HKG → KUL → DPS → HKT")
    val routeDescription = segments.joinToString(" → ") { segment ->
        MockData.getAirportName(segment.departure.iataCode)
    } + " → " + MockData.getAirportName(segments.last().arrival.iataCode)

    // Flight type name
    val flightTypeName = when (segments.size) {
        1 -> "Direct Flight"
        2 -> "Multi-Stop via ${MockData.getAirportName(segments[0].arrival.iataCode)}"
        else -> "Multi-City Detour"
    }

    // Total number of segments (connections)
    val numberOfConnections = segments.size - 1

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
                    text = "${offer.price.grandTotal.toDouble().toInt()} HKD",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecommended) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = routeDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (numberOfConnections == 0) "Non-stop" else "$numberOfConnections ${if (numberOfConnections == 1) "connection" else "connections"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = MockData.formatDuration(itinerary.duration),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Display airline info
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Airlines: ${offer.validatingAirlineCodes.joinToString(", ") { MockData.getAirlineName(it) }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
