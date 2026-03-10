package com.example.detour.data

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.util.Log
import com.android.volley.TimeoutError
import com.android.volley.DefaultRetryPolicy




/**
 * Simple wrapper around the Amadeus Flight Offers Search API
 * using the test environment.
 *
 * It does:
 *   1) Get OAuth access token
 *   2) Call /v2/shopping/flight-offers
 */
object FlightApi {

    private const val CLIENT_ID = "Liy3y6PMNl9uvA00UDG2zNs7dHy27EBi" //Amadeus API ID
    private const val CLIENT_SECRET = "Kxmx96sFgTjox7jg" //Amadeus API Secret

    private const val BASE_URL = "https://test.api.amadeus.com"

    // cache token in memory so we don't request it every time
    private var accessToken: String? = null
    private var tokenExpiryTimestamp: Long = 0L

    // === PUBLIC: search flights ===
    //
    // origin / destination : IATA codes, e.g. "HKG", "LAX"
    // departureDate        : "YYYY-MM-DD"
    // adults               : number of adult travellers (>=1)
    //
    // onSuccess gets a List<Map<String, String>> so you can
    // adapt it to whatever Flight model your UI already uses.
    fun searchFlights( //Searches for flight offers using the Amadeus Flight Offers API
        context: Context,
        origin: String,
        destination: String,
        departureDate: String,
        adults: Int = 1,
        onSuccess: (List<Map<String, String>>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        // Ensure we have a valid access token before making API calls
        ensureAccessToken(context) { token ->
            if (token == null) { //if token doesn't exist, raise error
                onError(IllegalStateException("Failed to get access token"))
                return@ensureAccessToken
            }

            //Create a Volley request queue using the app context
            val queue = Volley.newRequestQueue(context.applicationContext)

            //Build the Amadeus Flight Offers API URL
            val url =
                "$BASE_URL/v2/shopping/flight-offers" +
                        "?originLocationCode=$origin" + //original location airport code
                        "&destinationLocationCode=$destination" + //destination airport code
                        "&departureDate=$departureDate" + //YYYY-MM-DD Format
                        "&adults=$adults" + //number of passengers
                        "&currencyCode=HKD" + //currency in hkd since target users are based in HK
                        "&max=20" //limit results to up to 20 offers

            //Create the actual GET request
            val request = object : JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                { response ->
                    try {
                        // Convert the JSON into simple flight data (parsing)
                        val flights = parseFlightOffers(response)
                        // Send the results back to the caller
                        onSuccess(flights)
                    } catch (e: Exception) {
                        // If something goes wrong while reading the JSON
                        onError(e)
                    }
                },
                { error ->
                    // Try to read the HTTP status + error message
                    val status = error.networkResponse?.statusCode
                    val body = error.networkResponse?.data?.let { String(it) }

                    Log.e(
                        // Log the full error in case we need to debug later
                        "FlightApiOffers",
                        "Offers request FAILED. type=${error::class.java.simpleName} " +
                                "status=$status body=$body",
                        error
                    )

                    if (status != null) {
                        // Give the caller a readable error message
                        onError(IllegalStateException("HTTP $status\n$body"))
                    } else {
                        onError(error)
                    }
                }
            ) {
                // Add the "Authorization: Bearer <token>" header to the request
                override fun getHeaders(): MutableMap<String, String> =
                    hashMapOf("Authorization" to "Bearer $token")
            }

            //Set a longer timeout + 1 retry
            request.retryPolicy = DefaultRetryPolicy(
                15000, // 15 second timeout
                1,     // retry once
                1.0f
            )
            // Add request to queue, this sends it
            queue.add(request)
        }
    }


    private fun ensureAccessToken(
        context: Context,
        onResult: (String?) -> Unit
    ) {
        val now = System.currentTimeMillis()
        // If we already have a token and it has not expired yet,
        // just return it and don't call the API again
        if (accessToken != null && now < tokenExpiryTimestamp) {
            onResult(accessToken)
        } else {
            // Otherwise, request a new token from the server
            fetchAccessToken(context, onResult)
        }
    }

    private fun fetchAccessToken(
        context: Context,
        onResult: (String?) -> Unit
    ) {
        // Create a Volley request queue to send the network request
        val queue = Volley.newRequestQueue(context.applicationContext)
        // URL for getting an OAuth token from Amadeus
        val url = "$BASE_URL/v1/security/oauth2/token"

        // Create a POST request that expects a plain String response
        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->
                try {
                    // Log the raw response for debugging
                    Log.d("FlightApiToken", "Token response: $response")

                    // Turn the response string into a JSON object
                    val json = JSONObject(response)

                    // Read the "access_token" field from the JSON
                    val token = json.getString("access_token")

                    // Read how long the token is valid in seconds.
                    // Default to 1799 which is about 30 minutes if missing.
                    val expiresIn = json.optLong("expires_in", 1799L)

                    // Save token in memory
                    accessToken = token

                    // Calculate the time when the token should expire.
                    // We subtract 60 seconds so we refresh a bit early.
                    tokenExpiryTimestamp =
                        System.currentTimeMillis() + (expiresIn - 60) * 1000

                    // Pass the token back to whoever called this function
                    onResult(token)
                } catch (e: Exception) {
                    // If something goes wrong while reading the JSON
                    Log.e("FlightApiToken", "Error parsing token JSON", e)
                    onResult(null)
                }
            },
            { error ->
                // Try to get HTTP status code and body if available
                val status = error.networkResponse?.statusCode
                val body = error.networkResponse?.data?.let { String(it) }
                // Log the error for debugging
                Log.e(
                    "FlightApiToken",
                    "Token request FAILED. status=$status body=$body",
                    error
                )
                // Return null to show that we failed to get a token
                onResult(null)
            }
        ) {
            // Tell the server the type of data we are sending
            override fun getBodyContentType(): String =
                "application/x-www-form-urlencoded; charset=UTF-8"

            // Build the request body with grant_type, client_id, and client_secret
            override fun getBody(): ByteArray {
                val body =
                    "grant_type=client_credentials" +
                            "&client_id=$CLIENT_ID" +
                            "&client_secret=$CLIENT_SECRET"
                return body.toByteArray(Charsets.UTF_8)
            }
        }

        // Add the request to the queue to send it
        queue.add(request)
    }


    // Reads the JSON returned by the Amadeus API and converts it into a simple list of flight data that the app can easily use.
    //This function goes through each flight offer in the API response and extracts: price, route, departure, flight duration and number of stops
    private fun parseFlightOffers(response: JSONObject): List<Map<String, String>> {
        val result = mutableListOf<Map<String, String>>()

        // carrier code → airline name (if Amadeus sends it)
        val dictionaries = response.optJSONObject("dictionaries")
        val carriersDict = dictionaries?.optJSONObject("carriers")

        val dataArray = response.optJSONArray("data") ?: return result

        for (i in 0 until dataArray.length()) {
            val offer = dataArray.getJSONObject(i)

            val id = offer.optString("id", "")

            val priceObj = offer.optJSONObject("price")
            val totalPrice = priceObj?.optString("total", "") ?: ""

            val itineraries = offer.optJSONArray("itineraries")
            val firstItinerary = itineraries?.optJSONObject(0)
            val duration = firstItinerary?.optString("duration", "") ?: ""

            val segments = firstItinerary?.optJSONArray("segments")
            val firstSegment = segments?.optJSONObject(0)
            val lastSegment = segments?.optJSONObject((segments.length() - 1).coerceAtLeast(0))

            val dep = firstSegment?.optJSONObject("departure")
            val arr = lastSegment?.optJSONObject("arrival")

            val originCode = dep?.optString("iataCode", "") ?: ""
            val destCode = arr?.optString("iataCode", "") ?: ""
            val departureTime = dep?.optString("at", "") ?: ""
            val arrivalTime = arr?.optString("at", "") ?: ""

            val stops = (segments?.length() ?: 1) - 1

            //Build full route string from segments, avoiding duplicates
            val routeAirports = mutableListOf<String>()

            if (segments != null && segments.length() > 0) {
                for (j in 0 until segments.length()) {
                    val seg = segments.optJSONObject(j)
                    val segDep = seg?.optJSONObject("departure")
                    val segArr = seg?.optJSONObject("arrival")

                    val depCode = segDep?.optString("iataCode", "") ?: ""
                    val arrCode = segArr?.optString("iataCode", "") ?: ""

                    // first segment departure
                    if (j == 0 && depCode.isNotEmpty()) {
                        routeAirports.add(depCode)
                    }
                    // arrival, but no consecutive duplicates
                    if (arrCode.isNotEmpty() && (routeAirports.isEmpty() || routeAirports.last() != arrCode)) {
                        routeAirports.add(arrCode)
                    }
                }
            } else {
                // fallback if segments missing
                if (originCode.isNotEmpty()) routeAirports.add(originCode)
                if (destCode.isNotEmpty() && destCode != originCode) routeAirports.add(destCode)
            }

            val fullRoute = routeAirports.joinToString(" → ")

            //Collect airlines from segments
            val airlineCodes = mutableSetOf<String>()
            if (segments != null) {
                for (j in 0 until segments.length()) {
                    val seg = segments.optJSONObject(j)
                    val carrierCode = seg?.optString("carrierCode", "") ?: ""
                    if (carrierCode.isNotEmpty()) {
                        airlineCodes.add(carrierCode)
                    }
                }
            }

            // Map carrier codes to names if we have the dictionary
            val airlineNames = mutableListOf<String>()
            for (code in airlineCodes) {
                val name = carriersDict?.optString(code)
                if (!name.isNullOrEmpty()) {
                    airlineNames.add(name)
                }
            }

            val airlineDisplay = when {
                airlineNames.isNotEmpty() -> airlineNames.joinToString(" / ")
                airlineCodes.isNotEmpty() -> airlineCodes.joinToString(" / ")
                else -> ""
            }

            val map = mapOf(
                "id" to id,
                "origin" to originCode,
                "destination" to destCode,
                "departureTime" to departureTime,
                "arrivalTime" to arrivalTime,
                "duration" to duration,
                "price" to totalPrice,
                "stops" to stops.toString(),
                "fullRoute" to fullRoute,
                "airline" to airlineDisplay
            )

            result.add(map)
        }

        return result
    }

}

