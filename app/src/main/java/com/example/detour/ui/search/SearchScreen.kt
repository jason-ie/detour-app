package com.example.detour.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.detour.data.MockData
import com.google.accompanist.flowlayout.FlowRow
import java.text.SimpleDateFormat
import java.util.*

fun formatDateToApiString(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSearchClick: (String, String, Int, List<String>, String) -> Unit
) {
    var selectedOrigin by remember { mutableStateOf<MockData.City?>(null) }
    var selectedDestination by remember { mutableStateOf<MockData.City?>(null) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var tripDuration by remember { mutableIntStateOf(7) }

    // City selection state - only show region-relevant cities
    val stopoverCities = remember(selectedDestination) {
        selectedDestination?.let { dest ->
            // Show only cities from the same region as destination
            if (dest.region == "Southeast Asia") {
                MockData.PopularCities.southeastAsiaCities.filter { it.iataCode != dest.iataCode }
            } else {
                MockData.PopularCities.eastAsiaCities.filter { it.iataCode != dest.iataCode }
            }
        } ?: emptyList()
    }

    val selectedCities = remember { mutableStateListOf<String>() }

    // Update selected cities when destination changes - pre-select all
    LaunchedEffect(selectedDestination) {
        selectedCities.clear()
        selectedCities.addAll(stopoverCities.map { it.iataCode })
    }

    // Date formatter for display
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detour - Find Cheaper Routes") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Search Flights",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Origin Autocomplete
            CityAutocomplete(
                label = "Starting from (your home city)",
                selectedCity = selectedOrigin,
                onCitySelected = { selectedOrigin = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Destination Autocomplete
            CityAutocomplete(
                label = "Main destination",
                selectedCity = selectedDestination,
                onCitySelected = { selectedDestination = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // City Selection Section - Only show if destination is selected
            if (stopoverCities.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cities to visit along the way (optional stopovers)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // City chips in a flow layout
                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        stopoverCities.forEach { city ->
                            FilterChip(
                                selected = selectedCities.contains(city.iataCode),
                                onClick = {
                                    if (selectedCities.contains(city.iataCode)) {
                                        selectedCities.remove(city.iataCode)
                                    } else {
                                        selectedCities.add(city.iataCode)
                                    }
                                },
                                label = { Text(city.name) },
                                leadingIcon = if (selectedCities.contains(city.iataCode)) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date Picker Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            ) {
                OutlinedTextField(
                    value = selectedDateMillis?.let { dateFormatter.format(Date(it)) } ?: "",
                    onValueChange = { },
                    label = { Text("Departure Date") },
                    placeholder = { Text("Select date") },
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date"
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trip Duration Slider
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Trip Duration",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$tripDuration days",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = tripDuration.toFloat(),
                    onValueChange = { tripDuration = it.toInt() },
                    valueRange = 3f..14f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "3 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "14 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    selectedOrigin?.let { origin ->
                        selectedDestination?.let { destination ->

                            //GET_DATE: convert selectedDateMillis → "YYYY-MM-DD"
                            val apiDate = selectedDateMillis?.let {
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                sdf.format(Date(it))
                            } ?: ""

                            // SEND DATE INTO NAVIGATION
                            onSearchClick(
                                origin.iataCode,
                                destination.iataCode,
                                tripDuration,
                                selectedCities.toList(),
                                apiDate
                            )
                        }
                    }
                },

                enabled = selectedOrigin != null && selectedDestination != null && selectedDateMillis != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = when {
                        selectedOrigin == null || selectedDestination == null -> "Select origin and destination"
                        selectedDateMillis == null -> "Select a date"
                        else -> "Search Flights"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Find cheaper routes with strategic stopovers",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Only allow current day and future dates
                    return utcTimeMillis >= System.currentTimeMillis() - 86400000 // Allow today
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityAutocomplete(
    label: String,
    selectedCity: MockData.City?,
    onCitySelected: (MockData.City) -> Unit,
    modifier: Modifier = Modifier
) {
    // Initialize searchText from selectedCity so it persists
    var searchText by remember(selectedCity) {
        mutableStateOf(selectedCity?.let { "${it.name} (${it.iataCode})" } ?: "")
    }
    var expanded by remember { mutableStateOf(false) }
    val allCities = MockData.PopularCities.allCities

    // Filter cities based on search text - only show when user is typing
    val filteredCities = remember(searchText) {
        if (searchText.isEmpty()) {
            emptyList()
        } else {
            // Don't show dropdown if text exactly matches selected city
            val matchesSelected = selectedCity?.let {
                searchText == "${it.name} (${it.iataCode})"
            } ?: false

            if (matchesSelected) {
                emptyList()
            } else {
                allCities.filter { city ->
                    city.name.contains(searchText, ignoreCase = true) ||
                            city.iataCode.contains(searchText, ignoreCase = true)
                }
            }
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                expanded = it.isNotEmpty()
            },
            label = { Text(label) },
            placeholder = { Text("Type to search...") },
            modifier = Modifier.fillMaxWidth()
        )

        // Show results in a Card below the TextField
        if (expanded && filteredCities.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn {
                    items(filteredCities) { city ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCitySelected(city)
                                    searchText = "${city.name} (${city.iataCode})"
                                    expanded = false
                                }
                                .padding(16.dp)
                        ) {
                            Text(
                                text = city.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${city.iataCode} · ${city.region}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (city != filteredCities.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}