package com.example.omnigo.features.customer.home.presentation.viewmodel

import com.example.omnigo.features.customer.food.domain.model.Restaurant
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class HomeFilter {
    ALL,
    NEARBY,
    PROMOS,
    TOP_RATED
}

class HomeFilterCoordinator @Inject constructor() {

    fun filterAndSort(
        restaurants: List<Restaurant>,
        filter: HomeFilter,
        userLatitude: Double?,
        userLongitude: Double?
    ): List<Restaurant> {
        if (restaurants.isEmpty()) return emptyList()

        return when (filter) {
            HomeFilter.ALL -> restaurants

            HomeFilter.NEARBY -> {
                if (userLatitude != null && userLongitude != null) {
                    restaurants.sortedBy { restaurant ->
                        if (restaurant.hasCoordinates) {
                            calculateHaversineDistanceKm(
                                userLatitude,
                                userLongitude,
                                restaurant.latitude,
                                restaurant.longitude
                            )
                        } else {
                            Double.MAX_VALUE
                        }
                    }
                } else {
                    restaurants
                }
            }

            HomeFilter.TOP_RATED -> {
                val highRated = restaurants.filter { it.rating >= 4.8 }
                if (highRated.isNotEmpty()) {
                    highRated.sortedByDescending { it.rating }
                } else {
                    restaurants.sortedByDescending { it.rating }
                }
            }

            HomeFilter.PROMOS -> {
                // Prioritize open restaurants with complete menu or high rating
                restaurants.sortedWith(
                    compareByDescending<Restaurant> { it.isOpen }
                        .thenByDescending { it.menuItems.size }
                        .thenByDescending { it.rating }
                )
            }
        }
    }

    private fun calculateHaversineDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

