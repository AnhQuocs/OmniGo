package com.example.omnigo.features.customer.ride.domain.model

data class ActiveRideState(
    val bookingId: Long = 0L,
    val status: BookingStatus = BookingStatus.PENDING,
    val driverProfile: BookingDriverProfile? = null,
    val driverLiveLocation: DriverLiveLocation? = null,
    val startLatitude: Double = 0.0,
    val startLongitude: Double = 0.0,
    val endLatitude: Double = 0.0,
    val endLongitude: Double = 0.0,
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val price: Double = 0.0,
    val paymentMethod: String? = null,
    val liveDistanceKm: Double? = null,
    val liveEtaMinutes: Int? = null,
    val isSearchingDriver: Boolean = false,
    val isDriverProfileLoading: Boolean = false,
    val statusMessage: String? = null
) {
    val isTripActive: Boolean
        get() = status == BookingStatus.ACCEPTED ||
                status == BookingStatus.ARRIVED ||
                status == BookingStatus.IN_PROGRESS

    val isPending: Boolean
        get() = status == BookingStatus.PENDING

    val isFinished: Boolean
        get() = status == BookingStatus.COMPLETED ||
                status == BookingStatus.CANCELLED
}
