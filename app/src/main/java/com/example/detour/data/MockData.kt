package com.example.detour.data

object MockData {

    // Popular Cities for Stopover Selection
    data class City(
        val name: String,
        val iataCode: String,
        val region: String  // "Southeast Asia" or "East Asia"
    )

    object PopularCities {
        // Southeast Asia Cities (10)
        val southeastAsiaCities = listOf(
            City("Bali", "DPS", "Southeast Asia"),
            City("Kuala Lumpur", "KUL", "Southeast Asia"),
            City("Singapore", "SIN", "Southeast Asia"),
            City("Bangkok", "BKK", "Southeast Asia"),
            City("Jakarta", "CGK", "Southeast Asia"),
            City("Phuket", "HKT", "Southeast Asia"),
            City("Ho Chi Minh City", "SGN", "Southeast Asia"),
            City("Hanoi", "HAN", "Southeast Asia"),
            City("Manila", "MNL", "Southeast Asia"),
            City("Chiang Mai", "CNX", "Southeast Asia")
        )

        // East Asia Cities (6)
        val eastAsiaCities = listOf(
            City("Hong Kong", "HKG", "East Asia"),
            City("Taipei", "TPE", "East Asia"),
            City("Seoul", "ICN", "East Asia"),
            City("Tokyo", "NRT", "East Asia"),
            City("Osaka", "KIX", "East Asia"),
            City("Shanghai", "PVG", "East Asia")
        )

        // All cities combined
        val allCities = southeastAsiaCities + eastAsiaCities

        /**
         * Get suggested cities based on destination
         * Returns 5 most relevant cities for the destination
         */
        fun getSuggestedCities(destination: String): List<City> {
            // Extract airport code from destination (e.g., "Bali (DPS)" -> "DPS")
            val destCode = destination.substringAfterLast("(").substringBefore(")")

            // Southeast Asia destination codes
            val southeastAsiaDestinations = listOf("DPS", "HKT", "BKK", "SGN", "REP", "CNX", "MNL", "CGK", "HAN")

            return if (southeastAsiaDestinations.contains(destCode)) {
                // Suggest Southeast Asia cities
                southeastAsiaCities.take(5)
            } else {
                // Mix: 3 Southeast Asia + 2 East Asia
                southeastAsiaCities.take(3) + eastAsiaCities.take(2)
            }
        }
    }

    // Data classes matching Amadeus API structure
    data class FlightOffer(
        val id: String,
        val type: String = "flight-offer",
        val price: Price,
        val itineraries: List<Itinerary>,
        val numberOfBookableSeats: Int,
        val validatingAirlineCodes: List<String>
    )

    data class Price(
        val currency: String,
        val total: String,
        val grandTotal: String
    )

    data class Itinerary(
        val duration: String,  // ISO 8601 format: PT4H30M (4 hours 30 minutes)
        val segments: List<Segment>
    )

    data class Segment(
        val id: String,
        val departure: LocationInfo,
        val arrival: LocationInfo,
        val carrierCode: String,       // Airline code (e.g., "CX" = Cathay Pacific)
        val number: String,             // Flight number
        val aircraft: String?,
        val duration: String,
        val numberOfStops: Int
    )

    data class LocationInfo(
        val iataCode: String,           // Airport code (e.g., "HKG", "DPS")
        val terminal: String?,
        val at: String                  // ISO 8601 datetime: 2025-11-20T10:00:00
    )

    // Sample Flight Offers

    /**
     * Direct flight: HKG → Bali (DPS)
     * Price: 4000 HKD
     * Duration: 4h 30m
     */
    fun getDirectFlight(): FlightOffer {
        return FlightOffer(
            id = "1",
            type = "flight-offer",
            price = Price(
                currency = "HKD",
                total = "4000.00",
                grandTotal = "4000.00"
            ),
            itineraries = listOf(
                Itinerary(
                    duration = "PT4H30M",
                    segments = listOf(
                        Segment(
                            id = "1",
                            departure = LocationInfo(
                                iataCode = "HKG",
                                terminal = "1",
                                at = "2025-11-20T10:00:00"
                            ),
                            arrival = LocationInfo(
                                iataCode = "DPS",
                                terminal = "I",
                                at = "2025-11-20T14:30:00"
                            ),
                            carrierCode = "CX",
                            number = "777",
                            aircraft = "77W",
                            duration = "PT4H30M",
                            numberOfStops = 0
                        )
                    )
                )
            ),
            numberOfBookableSeats = 9,
            validatingAirlineCodes = listOf("CX")
        )
    }

    /**
     * Multi-stop route: HKG → Kuala Lumpur → Bali
     * Price: 3000 HKD
     * Duration: 9h 45m
     * Savings: 1000 HKD
     */
    fun getMultiStopKualaLumpur(): FlightOffer {
        return FlightOffer(
            id = "2",
            type = "flight-offer",
            price = Price(
                currency = "HKD",
                total = "3000.00",
                grandTotal = "3000.00"
            ),
            itineraries = listOf(
                Itinerary(
                    duration = "PT9H45M",
                    segments = listOf(
                        Segment(
                            id = "2",
                            departure = LocationInfo(
                                iataCode = "HKG",
                                terminal = "1",
                                at = "2025-11-20T08:00:00"
                            ),
                            arrival = LocationInfo(
                                iataCode = "KUL",
                                terminal = "M",
                                at = "2025-11-20T12:00:00"
                            ),
                            carrierCode = "AK",
                            number = "101",
                            aircraft = "320",
                            duration = "PT4H00M",
                            numberOfStops = 0
                        ),
                        Segment(
                            id = "3",
                            departure = LocationInfo(
                                iataCode = "KUL",
                                terminal = "M",
                                at = "2025-11-20T14:15:00"
                            ),
                            arrival = LocationInfo(
                                iataCode = "DPS",
                                terminal = "I",
                                at = "2025-11-20T17:45:00"
                            ),
                            carrierCode = "AK",
                            number = "202",
                            aircraft = "320",
                            duration = "PT3H30M",
                            numberOfStops = 0
                        )
                    )
                )
            ),
            numberOfBookableSeats = 7,
            validatingAirlineCodes = listOf("AK")
        )
    }

    /**
     * Multi-city Detour route: HKG → KUL → Bali → Phuket
     * Price: 2500 HKD
     * Duration: 12h 30m
     * Savings: 1500 HKD
     */
    fun getDetourRoute(): FlightOffer {
        return FlightOffer(
            id = "3",
            type = "flight-offer",
            price = Price(
                currency = "HKD",
                total = "2500.00",
                grandTotal = "2500.00"
            ),
            itineraries = listOf(
                Itinerary(
                    duration = "PT12H30M",
                    segments = listOf(
                        Segment(
                            id = "4",
                            departure = LocationInfo(
                                iataCode = "HKG",
                                terminal = "1",
                                at = "2025-11-20T08:00:00"
                            ),
                            arrival = LocationInfo(
                                iataCode = "KUL",
                                terminal = "M",
                                at = "2025-11-20T12:00:00"
                            ),
                            carrierCode = "AK",
                            number = "101",
                            aircraft = "320",
                            duration = "PT4H00M",
                            numberOfStops = 0
                        ),
                        Segment(
                            id = "5",
                            departure = LocationInfo(
                                iataCode = "KUL",
                                terminal = "M",
                                at = "2025-11-20T14:00:00"
                            ),
                            arrival = LocationInfo(
                                iataCode = "DPS",
                                terminal = "I",
                                at = "2025-11-20T17:30:00"
                            ),
                            carrierCode = "AK",
                            number = "202",
                            aircraft = "320",
                            duration = "PT3H30M",
                            numberOfStops = 0
                        ),
                        Segment(
                            id = "6",
                            departure = LocationInfo(
                                iataCode = "DPS",
                                terminal = "I",
                                at = "2025-11-20T18:45:00"
                            ),
                            arrival = LocationInfo(
                                iataCode = "HKT",
                                terminal = null,
                                at = "2025-11-20T20:30:00"
                            ),
                            carrierCode = "FD",
                            number = "303",
                            aircraft = "738",
                            duration = "PT1H45M",
                            numberOfStops = 0
                        )
                    )
                )
            ),
            numberOfBookableSeats = 5,
            validatingAirlineCodes = listOf("AK", "FD")
        )
    }

    /**
     * Get all flight offers
     */
    fun getAllFlightOffers(): List<FlightOffer> {
        return listOf(
            getDirectFlight(),
            getMultiStopKualaLumpur(),
            getDetourRoute()
        )
    }

    /**
     * Calculate savings compared to direct flight
     */
    fun calculateSavings(flightOffer: FlightOffer): Double {
        val directPrice = getDirectFlight().price.grandTotal.toDouble()
        val offerPrice = flightOffer.price.grandTotal.toDouble()
        return directPrice - offerPrice
    }

    /**
     * Get human-readable duration from ISO 8601 format
     * Example: PT4H30M → "4h 30m"
     */
    fun formatDuration(isoDuration: String): String {
        val hours = Regex("""(\d+)H""").find(isoDuration)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minutes = Regex("""(\d+)M""").find(isoDuration)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        return buildString {
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m")
        }.trim()
    }

    /**
     * Get airline name from carrier code
     */
    fun getAirlineName(carrierCode: String): String {
        return when (carrierCode) {
            "CX" -> "Cathay Pacific"
            "AK" -> "AirAsia"
            "FD" -> "Thai AirAsia"
            else -> carrierCode
        }
    }

    /**
     * Get airport name from IATA code
     */
    fun getAirportName(iataCode: String): String {
        return when (iataCode) {
            "HKG" -> "Hong Kong"
            "DPS" -> "Bali (Denpasar)"
            "KUL" -> "Kuala Lumpur"
            "HKT" -> "Phuket"
            else -> iataCode
        }
    }
}