package com.example.omnigo.features.customer.activity.domain.util

import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.FoodOrderItem
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `resolveFoodTimelineStep maps order statuses to 1 through 4`() {
        assertEquals(1, ActivityHistoryHelper.resolveFoodTimelineStep("PENDING"))
        assertEquals(1, ActivityHistoryHelper.resolveFoodTimelineStep("ACCEPTED"))
        assertEquals(2, ActivityHistoryHelper.resolveFoodTimelineStep("PREPARING"))
        assertEquals(2, ActivityHistoryHelper.resolveFoodTimelineStep("DRIVER_SEARCHING"))
        assertEquals(3, ActivityHistoryHelper.resolveFoodTimelineStep("DELIVERING"))
        assertEquals(3, ActivityHistoryHelper.resolveFoodTimelineStep("PICKED_UP"))
        assertEquals(4, ActivityHistoryHelper.resolveFoodTimelineStep("DELIVERED"))
        assertEquals(4, ActivityHistoryHelper.resolveFoodTimelineStep("COMPLETED"))
    }

    @Test
    fun `summarizeFoodItemsText concatenates item names and appends ellipsis when exceeding maxNames`() {
        val items = listOf(
            FoodOrderItem(1, 10, "Phở tái", 50000.0, 1, 50000.0, ""),
            FoodOrderItem(2, 11, "Quẩy giòn", 15000.0, 2, 30000.0, ""),
            FoodOrderItem(3, 12, "Trà đá chanh", 10000.0, 1, 10000.0, ""),
            FoodOrderItem(4, 13, "Trứng chần", 10000.0, 1, 10000.0, "")
        )

        val summary = ActivityHistoryHelper.summarizeFoodItemsText(items, maxNames = 3)
        assertEquals("Phở tái, Quẩy giòn, Trà đá chanh, ...", summary)
        assertEquals(5, ActivityHistoryHelper.totalItemQuantity(items))
    }

    @Test
    fun `calculateDeliveryWindow computes start and end time from createdAt and estimatedMinutes`() {
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
}

