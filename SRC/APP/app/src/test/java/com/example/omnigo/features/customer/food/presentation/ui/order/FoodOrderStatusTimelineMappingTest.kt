package com.example.omnigo.features.customer.food.presentation.ui.order

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodOrderStatusTimelineMappingTest {

    @Test
    fun `maps documented and backend statuses to ordered progress stages`() {
        assertEquals(1, "AWAITING_PAYMENT".toFoodOrderTimelineStep())
        assertEquals(1, "PENDING_PAYMENT".toFoodOrderTimelineStep())
        assertEquals(1, "ACCEPTED".toFoodOrderTimelineStep())
        assertEquals(2, "PREPARING".toFoodOrderTimelineStep())
        assertEquals(2, "READY_FOR_PICKUP".toFoodOrderTimelineStep())
        assertEquals(2, "NO_DRIVER_FOUND".toFoodOrderTimelineStep())
        assertEquals(3, "PICKED_UP".toFoodOrderTimelineStep())
        assertEquals(3, "DELIVERING".toFoodOrderTimelineStep())
        assertEquals(4, "COMPLETED".toFoodOrderTimelineStep())
        assertEquals(4, "DELIVERED".toFoodOrderTimelineStep())
    }

    @Test
    fun `treats cancellation and rejection as terminal without guessing unknown states`() {
        assertTrue("CANCELLED".isTerminalFoodOrderStatus())
        assertTrue("REJECTED".isTerminalFoodOrderStatus())
        assertTrue("COMPLETED".isTerminalFoodOrderStatus())
        assertFalse("NO_DRIVER_FOUND".isTerminalFoodOrderStatus())
        assertNull("FUTURE_STATUS".toFoodOrderTimelineStep())
        assertFalse("FUTURE_STATUS".isTerminalFoodOrderStatus())
    }
}
