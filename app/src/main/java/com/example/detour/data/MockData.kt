package com.example.detour.data

object MockData {

    data class FlightRoute(
        val routeName: String,
        val description: String,
        val price: Int,
        val stops: Int,
        val duration: String
    )

    fun getDirectFlight(): FlightRoute {
        return FlightRoute(
            routeName = "Direct Flight",
            description = "Hong Kong (HKG) → Bali (DPS)",
            price = 4000,
            stops = 0,
            duration = "5h 30m"
        )
    }

    fun getMultiStopRoute(): FlightRoute {
        return FlightRoute(
            routeName = "Multi-Stop Adventure",
            description = "HKG → Kuala Lumpur → Bali → Phuket → HKG",
            price = 2500,
            stops = 3,
            duration = "4 days"
        )
    }

    fun getSavings(): Int {
        return getDirectFlight().price - getMultiStopRoute().price
    }
}