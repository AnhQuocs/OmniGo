package com.example.omnigo.features.customer.food.data.mapper

import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderResponse
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FoodOrderMapperTest {

    private val gson = Gson()

    @Test
    fun `maps backend food order payload fields and preserves backend total`() {
        val response = gson.fromJson(
            """
                {
                  "id": 74,
                  "restaurantId": 12,
                  "restaurantName": "Sample Kitchen",
                  "restaurantAddress": "Main Street",
                  "driverId": 8,
                  "driverName": "Rider",
                  "driverPhone": "0900123456",
                  "vehiclePlate": "ABC-123",
                  "avatarUrl": "https://example.com/rider.png",
                  "status": "DELIVERING",
                  "totalPrice": 8123.0,
                  "deliveryFee": 456.0,
                  "deliveryAddress": "Customer Street",
                  "deliveryLatitude": 21.0,
                  "deliveryLongitude": 105.0,
                  "paymentMethod": "WALLET",
                  "isPaid": true,
                  "orderItems": [
                    {
                      "id": 3,
                      "menuItemId": 5,
                      "itemName": "Noodle Soup",
                      "price": 1200.0,
                      "quantity": 2,
                      "subtotal": 2400.0
                    }
                  ]
                }
            """.trimIndent(),
            FoodOrderResponse::class.java
        )

        val order = response.toDomain()

        assertEquals(74L, order.id)
        assertEquals("Customer Street", order.dropOffAddress)
        assertEquals(21.0, order.dropOffLatitude!!, 0.001)
        assertEquals(105.0, order.dropOffLongitude!!, 0.001)
        assertEquals("Rider", order.driverName)
        assertEquals("0900123456", order.driverPhone)
        assertEquals("ABC-123", order.driverVehiclePlate)
        assertEquals("https://example.com/rider.png", order.driverAvatarUrl)
        assertEquals(1, order.items.size)
        assertEquals(1200.0, order.items.single().itemPrice, 0.001)
        assertEquals(2400.0, order.items.single().subtotal, 0.001)
        assertEquals(PaymentMethod.WALLET, order.paymentMethod)
        assertEquals(true, order.isPaid)
        assertEquals(456.0, order.deliveryFee!!, 0.001)
        assertEquals(8123.0, order.totalPrice!!, 0.001)
    }

    @Test
    fun `supports documented aliases and leaves absent financial data unknown`() {
        val response = gson.fromJson(
            """
                {
                  "orderId": 75,
                  "dropOffAddress": "Alternate Address",
                  "items": [
                    {
                      "menuItemId": 6,
                      "itemPrice": 30.0,
                      "quantity": 1
                    }
                  ],
                  "paymentMethod": "FUTURE_METHOD"
                }
            """.trimIndent(),
            FoodOrderResponse::class.java
        )

        val order = response.toDomain()

        assertEquals(75L, order.id)
        assertEquals("Alternate Address", order.dropOffAddress)
        assertEquals(30.0, order.items.single().itemPrice, 0.001)
        assertNull(order.paymentMethod)
        assertNull(order.isPaid)
        assertNull(order.deliveryFee)
        assertNull(order.totalPrice)
        assertNull(order.estimatedDeliveryMinutes)
    }
}
