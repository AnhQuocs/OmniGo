package com.example.omnigo.features.customer.ride.data.remote.realtime

object RideStompDestinations {
    const val DRIVER_RIDE_MATCH = "/user/queue/driver/match"
    const val BOOKING_STATUS = "/user/queue/booking/status"
    const val DRIVER_LOCATION_TEMPLATE = "/topic/driver-location/{driverId}"

    fun driverLocation(driverId: Long): String {
        require(driverId > 0L) { "driverId must be positive" }
        return DRIVER_LOCATION_TEMPLATE.replace("{driverId}", driverId.toString())
    }
}
