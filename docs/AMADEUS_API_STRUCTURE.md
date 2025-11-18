# Amadeus Flight Offers Search API - Response Structure Documentation

**API Endpoint:** `GET https://test.api.amadeus.com/v2/shopping/flight-offers`
**Documentation:** [Amadeus Flight Offers Search API](https://developers.amadeus.com/self-service/category/flights/api-doc/flight-offers-search)
**OpenAPI Spec:** [FlightOffersSearch_v2_swagger_specification.json](https://github.com/amadeus4dev/amadeus-open-api-specification/blob/main/spec/json/FlightOffersSearch_v2_swagger_specification.json)

---

## Overview

The Amadeus Flight Offers Search API returns a list of **up to 250 flight-offer objects**, each containing complete itinerary information, pricing details, and booking requirements.

---

## Response Structure

### Root Response Object

```json
{
  "meta": {
    "count": 250,
    "links": {
      "self": "https://..."
    }
  },
  "data": [
    {
      // Array of flight-offer objects
    }
  ],
  "dictionaries": {
    "locations": {},
    "aircraft": {},
    "currencies": {},
    "carriers": {}
  }
}
```

---

## Flight Offer Object (data[])

Each flight offer contains the following key fields:

```json
{
  "type": "flight-offer",
  "id": "1",
  "source": "GDS",
  "instantTicketingRequired": false,
  "nonHomogeneous": false,
  "oneWay": false,
  "lastTicketingDate": "2025-11-20",
  "lastTicketingDateTime": "2025-11-20T23:59:00",
  "numberOfBookableSeats": 9,
  "itineraries": [],
  "price": {},
  "pricingOptions": {},
  "validatingAirlineCodes": ["AA"],
  "travelerPricings": []
}
```

### Key Fields

| Field | Type | Description |
|-------|------|-------------|
| `type` | string | Always "flight-offer" |
| `id` | string | Unique identifier for this offer |
| `source` | string | "GDS" (Global Distribution System) |
| `instantTicketingRequired` | boolean | Must be ticketed immediately after booking |
| `nonHomogeneous` | boolean | Different fare families for outbound/return |
| `oneWay` | boolean | One-way flight indicator |
| `lastTicketingDate` | string | Last date to issue ticket (YYYY-MM-DD) |
| `numberOfBookableSeats` | number | Available seats (1-9, or actual count) |
| `itineraries` | array | Flight segments (1 for one-way, 2 for round-trip) |
| `price` | object | Complete pricing information |
| `validatingAirlineCodes` | array | Airlines that can validate this fare |

---

## Itineraries Structure

Each flight offer contains 1-2 itineraries (1 for one-way, 2 for round-trip):

```json
{
  "itineraries": [
    {
      "duration": "PT2H10M",  // ISO 8601 duration format (2 hours 10 minutes)
      "segments": [
        {
          "departure": {
            "iataCode": "HKG",
            "terminal": "1",
            "at": "2025-11-20T10:00:00"
          },
          "arrival": {
            "iataCode": "DPS",
            "terminal": "I",
            "at": "2025-11-20T15:10:00"
          },
          "carrierCode": "CX",
          "number": "777",
          "aircraft": {
            "code": "77W"
          },
          "operating": {
            "carrierCode": "CX"
          },
          "duration": "PT2H10M",
          "id": "1",
          "numberOfStops": 0,
          "blacklistedInEU": false
        }
      ]
    }
  ]
}
```

### Segment Fields

| Field | Type | Description |
|-------|------|-------------|
| `departure` | object | Departure airport, terminal, timestamp |
| `arrival` | object | Arrival airport, terminal, timestamp |
| `carrierCode` | string | IATA airline code (e.g., "CX" for Cathay Pacific) |
| `number` | string | Flight number |
| `aircraft.code` | string | Aircraft type code (e.g., "77W" for Boeing 777-300ER) |
| `operating.carrierCode` | string | Actual operating airline (if codeshare) |
| `duration` | string | Segment duration in ISO 8601 format |
| `id` | string | Segment identifier |
| `numberOfStops` | integer | **Number of stops (0 = direct flight)** |
| `blacklistedInEU` | boolean | Banned in EU for safety reasons |

### Departure/Arrival Object

```json
{
  "iataCode": "HKG",      // Airport code
  "terminal": "1",        // Terminal (optional)
  "at": "2025-11-20T10:00:00"  // ISO 8601 datetime
}
```

---

## Price Object Structure

Complete pricing breakdown:

```json
{
  "price": {
    "currency": "HKD",
    "total": "4000.00",
    "base": "3500.00",
    "fees": [
      {
        "amount": "50.00",
        "type": "TICKETING"
      }
    ],
    "taxes": [
      {
        "amount": "450.00",
        "code": "YQ"
      }
    ],
    "grandTotal": "4000.00"
  }
}
```

### Price Fields

| Field | Type | Description |
|-------|------|-------------|
| `currency` | string | ISO currency code (e.g., "HKD", "USD") |
| `total` | string | Total fare (base + taxes + fees) |
| `base` | string | Base fare (before taxes/fees) |
| `grandTotal` | string | Final amount to charge |
| `fees` | array | Booking/service fees |
| `taxes` | array | Airport taxes, fuel surcharges, etc. |

---

## Direct vs Multi-Stop Flights

### How to Differentiate:

**Direct Flight (Non-stop):**
```json
{
  "numberOfStops": 0,
  "stops": []
}
```

**Multi-Stop Flight (with layovers):**
```json
{
  "numberOfStops": 2,
  "stops": [
    {
      "iataCode": "KUL",
      "duration": "PT2H30M",
      "arrivalAt": "2025-11-20T12:30:00",
      "departureAt": "2025-11-20T15:00:00"
    },
    {
      "iataCode": "SIN",
      "duration": "PT1H45M",
      "arrivalAt": "2025-11-20T17:30:00",
      "departureAt": "2025-11-20T19:15:00"
    }
  ]
}
```

**Multi-Segment Flight (with connections):**
- Multiple objects in `segments` array
- Each segment is a separate flight
- Requires plane change

---

## Example: HKG → Bali Comparison

### Direct Flight Example

```json
{
  "type": "flight-offer",
  "id": "1",
  "oneWay": true,
  "itineraries": [
    {
      "duration": "PT4H30M",
      "segments": [
        {
          "departure": {
            "iataCode": "HKG",
            "at": "2025-11-20T10:00:00"
          },
          "arrival": {
            "iataCode": "DPS",
            "at": "2025-11-20T15:30:00"
          },
          "carrierCode": "CX",
          "number": "777",
          "numberOfStops": 0
        }
      ]
    }
  ],
  "price": {
    "currency": "HKD",
    "total": "4000.00",
    "grandTotal": "4000.00"
  }
}
```

### Multi-City Route Example (HKG → KUL → DPS → Phuket)

```json
{
  "type": "flight-offer",
  "id": "2",
  "oneWay": true,
  "itineraries": [
    {
      "duration": "PT12H45M",
      "segments": [
        {
          "departure": {
            "iataCode": "HKG",
            "at": "2025-11-20T08:00:00"
          },
          "arrival": {
            "iataCode": "KUL",
            "at": "2025-11-20T12:00:00"
          },
          "carrierCode": "AK",
          "number": "101",
          "numberOfStops": 0
        },
        {
          "departure": {
            "iataCode": "KUL",
            "at": "2025-11-20T14:30:00"
          },
          "arrival": {
            "iataCode": "DPS",
            "at": "2025-11-20T17:30:00"
          },
          "carrierCode": "AK",
          "number": "202",
          "numberOfStops": 0
        },
        {
          "departure": {
            "iataCode": "DPS",
            "at": "2025-11-20T18:45:00"
          },
          "arrival": {
            "iataCode": "HKT",
            "at": "2025-11-20T20:45:00"
          },
          "carrierCode": "FD",
          "number": "303",
          "numberOfStops": 0
        }
      ]
    }
  ],
  "price": {
    "currency": "HKD",
    "total": "2500.00",
    "grandTotal": "2500.00"
  }
}
```

**Savings Calculation:** 4000 - 2500 = **1500 HKD saved**

---

## Mapping to Our App's Data Models

### Current MockData.kt Structure

Our app currently uses:
```kotlin
data class FlightRoute(
    val origin: String,
    val destination: String,
    val stops: List<String>,
    val price: Double,
    val savings: Double?
)
```

### Recommended Enhanced Structure

To properly map Amadeus API responses:

```kotlin
data class FlightOffer(
    val id: String,
    val price: Price,
    val itineraries: List<Itinerary>,
    val validatingAirlineCodes: List<String>,
    val numberOfBookableSeats: Int
)

data class Price(
    val currency: String,
    val total: String,
    val base: String?,
    val grandTotal: String
)

data class Itinerary(
    val duration: String,  // ISO 8601 format
    val segments: List<Segment>
)

data class Segment(
    val departure: Airport,
    val arrival: Airport,
    val carrierCode: String,
    val number: String,
    val aircraft: String?,
    val duration: String,
    val numberOfStops: Int
)

data class Airport(
    val iataCode: String,
    val terminal: String?,
    val dateTime: String  // ISO 8601 format
)
```

---

## Key Insights for Detour App

### 1. **Total Trip Duration**
- Use `itinerary.duration` to calculate total travel time
- Compare direct vs multi-stop total duration

### 2. **Segment Count = Connection Count**
- Direct flight: 1 segment
- 1 connection: 2 segments
- 2 connections: 3 segments
- Multi-city (like Detour concept): 3+ segments with different destinations

### 3. **Layover Calculation**
- Compare `segments[n].arrival.at` with `segments[n+1].departure.at`
- Difference = layover time

### 4. **Price Comparison**
- Use `price.grandTotal` for actual comparison
- Calculate savings: `directFlight.grandTotal - multiStopFlight.grandTotal`

### 5. **Unique Detour Feature**
- Standard API returns flights with **same origin and destination**
- Detour's innovation: Multi-city routes (HKG → KUL → DPS → HKT → HKG)
- This requires combining multiple one-way searches or using multi-city search

---

## API Query Parameters

To search for flights:

```
GET /v2/shopping/flight-offers?
  originLocationCode=HKG
  &destinationLocationCode=DPS
  &departureDate=2025-11-20
  &adults=1
  &max=250
```

For multi-city (Detour use case):
```json
POST /v2/shopping/flight-offers
{
  "originDestinations": [
    {
      "id": "1",
      "originLocationCode": "HKG",
      "destinationLocationCode": "KUL",
      "departureDateTimeRange": {
        "date": "2025-11-20"
      }
    },
    {
      "id": "2",
      "originLocationCode": "KUL",
      "destinationLocationCode": "DPS",
      "departureDateTimeRange": {
        "date": "2025-11-20"
      }
    },
    {
      "id": "3",
      "originLocationCode": "DPS",
      "destinationLocationCode": "HKT",
      "departureDateTimeRange": {
        "date": "2025-11-21"
      }
    }
  ],
  "travelers": [{"id": "1", "travelerType": "ADULT"}],
  "sources": ["GDS"]
}
```

---

## Additional Resources

1. **Official API Documentation:**
   https://developers.amadeus.com/self-service/category/flights/api-doc/flight-offers-search

2. **OpenAPI Specification (JSON Schema):**
   https://github.com/amadeus4dev/amadeus-open-api-specification

3. **SDK Examples:**
   - Node.js: https://github.com/amadeus4dev/amadeus-node
   - Python: https://github.com/amadeus4dev/amadeus-python
   - Java: https://github.com/amadeus4dev/amadeus-java

4. **Testing:**
   Use the Amadeus Self-Service Test API: `https://test.api.amadeus.com/v2/shopping/flight-offers`

---

## Next Steps for Our App

1. **Update MockData.kt** to match Amadeus response structure
2. **Enhance data models** with proper classes (FlightOffer, Itinerary, Segment, etc.)
3. **Build API integration** using Retrofit + Amadeus API
4. **Implement comparison logic** to identify cheaper multi-city routes
5. **Add algorithm** to calculate savings and highlight best options

---

**Last Updated:** November 17, 2025
**API Version:** v2
