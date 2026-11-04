package com.example.omnigo.features.driver.trips.domain.model

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class DriverHistoryModelsTest {

    @Test
    fun `Ride netEarnings uses actual platform fee instead of 80 percent hardcode`() {
        val bookingWithActualFee = Booking(
            bookingId = 1L,
            price = 100000.0,
            platformFee = 15000.0, // 15% thực tế
            status = BookingStatus.COMPLETED
        )

        val rideItem = DriverHistoryItem.Ride(bookingWithActualFee)

        // Không dùng 80% (80,000đ), mà dùng phí thực tế: 100,000 - 15,000 = 85,000đ
        assertEquals(85000.0, rideItem.netEarnings, 0.01)
    }

    @Test
    fun `Food netEarnings uses actual platform fee instead of 80 percent hardcode`() {
        val orderWithActualEarnings = DriverDeliveryOrder(
            orderId = 2L,
            deliveryFee = 30000.0,
            driverEarnings = 26000.0, // Thu nhập thực tế từ hệ thống
            status = DriverDeliveryStatus.COMPLETED
        )

        val foodItem = DriverHistoryItem.Food(orderWithActualEarnings)

        // Phải lấy 26,000đ từ driverEarnings thực tế, không dùng 30,000 * 0.80 = 24,000đ
        assertEquals(26000.0, foodItem.netEarnings, 0.01)
    }
}

