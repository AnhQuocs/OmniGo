package com.example.omnigo.features.customer.activity.domain.util

import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.FoodOrderItem
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class ActivityHistoryHelperTest {

    private fun sampleOrder(
        id: Long = 1L,
        status: String = "PREPARING",
        createdAt: String = "2026-10-20T09:30:00Z",
        estimatedMinutes: Int? = 25,
        items: List<FoodOrderItem> = emptyList()
    ): FoodOrder {
        return FoodOrder(
            id = id,
            customerId = 10L,
            driverId = 20L,
            restaurantId = 100L,
            restaurantName = "Phở Thìn Lò Đúc",
            restaurantAddress = "Lò Đúc, Hai Bà Trưng, Hà Nội",
            status = status,
            paymentMethod = PaymentMethod.CASH,
            isPaid = false,
            itemsPrice = 95000.0,
            deliveryFee = 15000.0,
            totalPrice = 110000.0,
            dropOffAddress = "219 Cầu Giấy",
            dropOffLatitude = 21.03,
            dropOffLongitude = 105.79,
            note = "",
            items = items,
            estimatedDeliveryMinutes = estimatedMinutes,
            createdAt = createdAt
        )
    }

    @Test
    fun `isOngoingFoodOrder and isHistoryFoodOrder classify statuses accurately`() {
        val ongoing = sampleOrder(status = "PREPARING")
        val delivered = sampleOrder(status = "DELIVERED")
        val cancelled = sampleOrder(status = "CANCELLED")

        assertTrue(ActivityHistoryHelper.isOngoingFoodOrder(ongoing))
        assertFalse(ActivityHistoryHelper.isHistoryFoodOrder(ongoing))

        assertFalse(ActivityHistoryHelper.isOngoingFoodOrder(delivered))
        assertTrue(ActivityHistoryHelper.isHistoryFoodOrder(delivered))

        assertFalse(ActivityHistoryHelper.isOngoingFoodOrder(cancelled))
        assertTrue(ActivityHistoryHelper.isHistoryFoodOrder(cancelled))
    }

    @Test
    fun `resolveFoodTimelineStep maps status to expected stage`() {
        assertEquals(1, ActivityHistoryHelper.resolveFoodTimelineStep("PENDING"))
        assertEquals(1, ActivityHistoryHelper.resolveFoodTimelineStep("ACCEPTED"))
        assertEquals(2, ActivityHistoryHelper.resolveFoodTimelineStep("PREPARING"))
        assertEquals(2, ActivityHistoryHelper.resolveFoodTimelineStep("DRIVER_SEARCHING"))
        assertEquals(3, ActivityHistoryHelper.resolveFoodTimelineStep("DELIVERING"))
        assertEquals(3, ActivityHistoryHelper.resolveFoodTimelineStep("PICKED_UP"))
        assertEquals(4, ActivityHistoryHelper.resolveFoodTimelineStep("DELIVERED"))
        assertEquals(4, ActivityHistoryHelper.resolveFoodTimelineStep("COMPLETED"))
        assertEquals(1, ActivityHistoryHelper.resolveFoodTimelineStep("UNKNOWN_STATUS"))
    }

    @Test
    fun `summarizeFoodItemsText handles empty and truncated items`() {
        val items = listOf(
            FoodOrderItem(id = 1L, menuItemId = 101L, itemName = "Phở Bò", itemPrice = 45000.0, quantity = 2),
            FoodOrderItem(id = 2L, menuItemId = 102L, itemName = "Quẩy", itemPrice = 5000.0, quantity = 3)
        )

        val summary = ActivityHistoryHelper.summarizeFoodItemsText(items, maxNames = 3)
        assertEquals("Phở Bò, Quẩy", summary)
        assertEquals(5, ActivityHistoryHelper.totalItemQuantity(items))
    }

    @Test
    fun `calculateDeliveryWindow uses base time plus eta range`() {
        val zone = ZoneId.of("UTC")
        val order = sampleOrder(
            status = "PREPARING",
            createdAt = "2026-10-20T09:50:00Z",
            estimatedMinutes = 25
        )

        val window = ActivityHistoryHelper.calculateDeliveryWindow(order, zoneId = zone)
        assertEquals("10:10", window.startTimeText)
        assertEquals("10:15", window.endTimeText)
        assertEquals(25, window.maxMinutes)
    }

    @Test
    fun `groupFoodOrdersByMonth groups and sorts descending by year and month`() {
        val zone = ZoneId.of("UTC")
        val octOrder1 = sampleOrder(id = 1L, status = "DELIVERED", createdAt = "2026-10-20T12:28:00Z")
        val octOrder2 = sampleOrder(id = 2L, status = "CANCELLED", createdAt = "2026-10-16T09:12:00Z")
        val sepOrder = sampleOrder(id = 3L, status = "DELIVERED", createdAt = "2026-09-28T11:20:00Z")

        val groups = ActivityHistoryHelper.groupFoodOrdersByMonth(
            listOf(sepOrder, octOrder1, octOrder2),
            zoneId = zone
        )

        assertEquals(2, groups.size)
        assertEquals(2026, groups[0].year)
        assertEquals(10, groups[0].month)
        assertEquals(2, groups[0].items.size)

        assertEquals(2026, groups[1].year)
        assertEquals(9, groups[1].month)
        assertEquals(1, groups[1].items.size)
    }

    @Test
    fun `parseZonedDateTime parses both ISO instant and local date time without error`() {
        val zone = ZoneId.of("UTC")
        val instantDate = ActivityHistoryHelper.parseZonedDateTime("2026-10-20T12:30:00Z", zone)
        assertNotNull(instantDate)
        assertEquals(2026, instantDate?.year)
        assertEquals(10, instantDate?.monthValue)
        assertEquals(20, instantDate?.dayOfMonth)
        assertEquals(12, instantDate?.hour)

        val localDateTime = ActivityHistoryHelper.parseZonedDateTime("2026-10-20T12:30:00", zone)
        assertNotNull(localDateTime)
        assertEquals(2026, localDateTime?.year)
        assertEquals(10, localDateTime?.monthValue)
        assertEquals(20, localDateTime?.dayOfMonth)
        assertEquals(12, localDateTime?.hour)

        assertNull(ActivityHistoryHelper.parseZonedDateTime(null, zone))
        assertNull(ActivityHistoryHelper.parseZonedDateTime("   ", zone))
    }

    @Test
    fun `formatCompactDateAndTime formats dates properly`() {
        val zone = ZoneId.of("UTC")
        val formatted = ActivityHistoryHelper.formatCompactDateAndTime("2026-10-20T12:30:00", zone)
        assertEquals("20/10/2026 • 12:30", formatted)
    }
}
