package com.example.omnigo.features.customer.food.data.remote.realtime

object FoodStompDestinations {
    const val DRIVER_FOOD_MATCH = "/user/queue/driver/food-match"
    const val FOOD_OFFERS = DRIVER_FOOD_MATCH
    const val FOOD_ORDER_ASSIGNMENT = "/user/queue/food-order"
    const val FOOD_ORDER_ASSIGNMENTS = FOOD_ORDER_ASSIGNMENT
    const val DRIVER_LOCATION_TEMPLATE = "/topic/driver-location/{driverId}"

    fun driverLocation(driverId: Long): String {
        require(driverId > 0) { "driverId must be positive" }
        return DRIVER_LOCATION_TEMPLATE.replace("{driverId}", driverId.toString())
    }

    // Official destination for full-lifecycle order status events (Customer and Driver)
    const val FOOD_ORDER_STATUS = "/user/queue/food-order/status"

    fun foodOrderStatus(orderId: Long? = null): String {
        return FOOD_ORDER_STATUS
    }
}
