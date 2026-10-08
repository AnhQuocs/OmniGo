package com.example.omnigo.features.customer.food.data.mapper

import com.example.omnigo.features.customer.food.domain.model.FoodOrderCreateCommand
import com.example.omnigo.features.customer.food.domain.model.FoodOrderCreateItemCommand
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Test

class FoodOrderRequestMapperTest {

    @Test
    fun `maps domain command to documented create order request`() {
        val request = FoodOrderCreateCommand(
            restaurantId = 20L,
            dropOffAddress = "Delivery Street",
            dropOffLatitude = 21.5,
            dropOffLongitude = 105.5,
            items = listOf(FoodOrderCreateItemCommand(44L, 3, "No chili")),
            note = "Call first",
            paymentMethod = PaymentMethod.MOMO
        ).toRequest()

        assertEquals(20L, request.restaurantId)
        assertEquals("Delivery Street", request.dropOffAddress)
        assertEquals(21.5, request.dropOffLatitude, 0.001)
        assertEquals(105.5, request.dropOffLongitude, 0.001)
        assertEquals("Call first", request.note)
        assertEquals("MOMO", request.paymentMethod)
        assertEquals(44L, request.items.single().menuItemId)
        assertEquals(3, request.items.single().quantity)
        assertEquals("No chili", request.items.single().note)
    }
}
