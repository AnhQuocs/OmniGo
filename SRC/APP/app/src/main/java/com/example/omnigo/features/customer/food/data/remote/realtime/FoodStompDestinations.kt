package com.example.omnigo.features.customer.food.data.remote.realtime

object FoodStompDestinations {
    const val FOOD_OFFERS = "/user/queue/food-offers"
    const val FOOD_ORDER_ASSIGNMENT = "/user/queue/food-order"
    const val DRIVER_LOCATION_TEMPLATE = "/topic/driver-location/{driverId}"

    fun driverLocation(driverId: Long): String {
        require(driverId > 0) { "driverId must be positive" }
        return DRIVER_LOCATION_TEMPLATE.replace("{driverId}", driverId.toString())
    }

    // The backend has not confirmed a destination for full-lifecycle order status events.
}
