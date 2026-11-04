package com.example.omnigo.features.driver.trips.domain.model

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import java.time.LocalDate

enum class DriverTripFilter {
    ALL,
    RIDE,
    FOOD
}

sealed class DriverHistoryItem {
    abstract val id: Long
    abstract val timestampStr: String?
    abstract val netEarnings: Double
    abstract val isSuccess: Boolean

    data class Ride(
        val booking: Booking
    ) : DriverHistoryItem() {
        override val id: Long get() = booking.bookingId
        override val timestampStr: String? get() = booking.completedAt ?: booking.createdAt
        override val netEarnings: Double get() = booking.actualEarnings
        override val isSuccess: Boolean get() = booking.status.name == "COMPLETED"
    }

    data class Food(
        val order: DriverDeliveryOrder
    ) : DriverHistoryItem() {
        override val id: Long get() = order.orderId
        override val timestampStr: String? get() = order.createdAt
        override val netEarnings: Double get() = order.actualEarnings
        override val isSuccess: Boolean get() = order.status.name == "COMPLETED"
    }
}

data class DriverHistoryDayGroup(
    val title: String,
    val date: LocalDate,
    val totalTrips: Int,
    val totalEarnings: Double,
    val items: List<DriverHistoryItem>
)
