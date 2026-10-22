package com.example.omnigo.features.customer.food.data.remote.realtime

object FoodStompDestinations {
    const val FOOD_OFFERS = "/user/queue/food-offers"
    const val FOOD_ORDER_ASSIGNMENT = "/user/queue/food-order"
    const val FOOD_ORDER_ASSIGNMENTS = FOOD_ORDER_ASSIGNMENT
    const val DRIVER_LOCATION_TEMPLATE = "/topic/driver-location/{driverId}"

    fun driverLocation(driverId: Long): String {
        require(driverId > 0) { "driverId must be positive" }
        return DRIVER_LOCATION_TEMPLATE.replace("{driverId}", driverId.toString())
    }

    // Candidate destination for full-lifecycle order status events (pending BE confirmation)
    const val FOOD_ORDER_STATUS_TEMPLATE = "/topic/food-orders/{orderId}/status"

    fun foodOrderStatus(orderId: Long): String {
        require(orderId > 0) { "orderId must be positive" }
        return FOOD_ORDER_STATUS_TEMPLATE.replace("{orderId}", orderId.toString())
    }
}
