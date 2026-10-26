package com.example.omnigo.features.customer.ride.presentation.viewmodel

sealed interface RideBookingEvent {
    data class ShowToast(val message: String) : RideBookingEvent
    data class BookingCreated(val bookingId: Long) : RideBookingEvent
    data object BookingCancelled : RideBookingEvent
}
