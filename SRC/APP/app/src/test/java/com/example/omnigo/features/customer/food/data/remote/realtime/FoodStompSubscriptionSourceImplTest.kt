package com.example.omnigo.features.customer.food.data.remote.realtime

import app.cash.turbine.test
import com.example.omnigo.core.websocket.StompClient
import com.example.omnigo.core.websocket.StompClientManager
import com.example.omnigo.core.websocket.StompCommand
import com.example.omnigo.core.websocket.StompFrame
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FoodStompSubscriptionSourceImplTest {

    private val mockBookingClient = mockk<StompClient>()
    private val mockLocationClient = mockk<StompClient>()
    private val mockManager = mockk<StompClientManager>(relaxed = true)
    private val gson = Gson()

    private lateinit var source: FoodStompSubscriptionSourceImpl

    @Before
    fun setup() {
        every { mockManager.bookingClient } returns mockBookingClient
        every { mockManager.locationClient } returns mockLocationClient
        source = FoodStompSubscriptionSourceImpl(mockManager, gson)
    }

    @Test
    fun `subscribeToFoodOrderAssignments parses assignment event dto`() = runTest {
        val payload = """
            {
                "orderId": 42,
                "driverId": 7,
                "driverName": "Nguyen Van A",
                "driverPhone": "0987654321",
                "vehiclePlate": "29A-12345"
            }
        """.trimIndent()
        val frame = StompFrame(
            command = StompCommand.MESSAGE,
            headers = mapOf("destination" to FoodStompDestinations.FOOD_ORDER_ASSIGNMENTS),
            body = payload
        )

        every { mockBookingClient.subscribe(FoodStompDestinations.FOOD_ORDER_ASSIGNMENTS) } returns flowOf(frame)

        source.subscribeToFoodOrderAssignments().test {
            val event = awaitItem()
            assertEquals(42L, event.orderId)
            assertEquals(7L, event.driverId)
            assertEquals("Nguyen Van A", event.driverName)
            assertEquals("0987654321", event.driverPhone)
            assertEquals("29A-12345", event.vehiclePlate)
            awaitComplete()
        }
    }

    @Test
    fun `subscribeToFoodOrderStatus subscribes to official queue and parses status event dto`() = runTest {
        val payload = """{"orderId": 42, "status": "CONFIRMED", "timestamp": "2026-10-22T09:00:00Z"}"""
        val destination = FoodStompDestinations.FOOD_ORDER_STATUS
        assertEquals("/user/queue/food-order/status", destination)

        val frame = StompFrame(
            command = StompCommand.MESSAGE,
            headers = mapOf("destination" to destination),
            body = payload
        )

        every { mockBookingClient.subscribe(destination) } returns flowOf(frame)

        source.subscribeToFoodOrderStatus(42L).test {
            val event = awaitItem()
            assertEquals(42L, event.orderId)
            assertEquals("CONFIRMED", event.status)
            assertEquals("2026-10-22T09:00:00Z", event.timestamp)
            awaitComplete()
        }
    }

    @Test
    fun `subscribeToFoodOrderStatus filters out events belonging to other orders`() = runTest {
        val matchingPayload = """{"orderId": 42, "status": "CONFIRMED", "timestamp": "2026-10-22T09:00:00Z"}"""
        val otherPayload = """{"orderId": 99, "status": "DELIVERED", "timestamp": "2026-10-22T09:05:00Z"}"""
        val destination = FoodStompDestinations.FOOD_ORDER_STATUS

        val frameOther = StompFrame(
            command = StompCommand.MESSAGE,
            headers = mapOf("destination" to destination),
            body = otherPayload
        )
        val frameMatch = StompFrame(
            command = StompCommand.MESSAGE,
            headers = mapOf("destination" to destination),
            body = matchingPayload
        )

        every { mockBookingClient.subscribe(destination) } returns flowOf(frameOther, frameMatch)

        source.subscribeToFoodOrderStatus(42L).test {
            val event = awaitItem()
            assertEquals(42L, event.orderId)
            assertEquals("CONFIRMED", event.status)
            awaitComplete()
        }
    }

    @Test
    fun `subscribeToDriverLocation parses location event dto from location client`() = runTest {
        val payload = """
            {
                "driverId": 7,
                "latitude": 10.7769,
                "longitude": 106.7009,
                "bearing": 180.0,
                "speed": 35.5,
                "timestamp": "2026-10-22T09:00:05Z"
            }
        """.trimIndent()
        val destination = FoodStompDestinations.driverLocation(7L)
        val frame = StompFrame(
            command = StompCommand.MESSAGE,
            headers = mapOf("destination" to destination),
            body = payload
        )

        every { mockLocationClient.subscribe(destination) } returns flowOf(frame)

        source.subscribeToDriverLocation(7L).test {
            val event = awaitItem()
            assertEquals(7L, event.driverId)
            assertEquals(10.7769, event.latitude!!, 0.0001)
            assertEquals(106.7009, event.longitude!!, 0.0001)
            assertEquals(180.0, event.bearing!!, 0.0001)
            assertEquals(35.5, event.speed!!, 0.0001)
            awaitComplete()
        }
    }

    @Test
    fun `subscribeToFoodOffers parses food offer event dto`() = runTest {
        val payload = """
            {
                "orderId": 42,
                "restaurantName": "Pho Bo",
                "restaurantAddress": "123 Le Loi",
                "dropOffAddress": "456 Nguyen Hue",
                "deliveryFee": 15000.0,
                "totalPrice": 75000.0
            }
        """.trimIndent()
        val frame = StompFrame(
            command = StompCommand.MESSAGE,
            headers = mapOf("destination" to FoodStompDestinations.FOOD_OFFERS),
            body = payload
        )

        every { mockBookingClient.subscribe(FoodStompDestinations.FOOD_OFFERS) } returns flowOf(frame)

        source.subscribeToFoodOffers().test {
            val event = awaitItem()
            assertEquals(42L, event.orderId)
            assertEquals("Pho Bo", event.restaurantName)
            assertEquals("123 Le Loi", event.restaurantAddress)
            assertEquals("456 Nguyen Hue", event.dropOffAddress)
            assertEquals(15000.0, event.deliveryFee!!, 0.001)
            assertEquals(75000.0, event.totalPrice!!, 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `malformed json payloads are safely dropped without throwing`() = runTest {
        val badFrame = StompFrame(
            command = StompCommand.MESSAGE,
            headers = mapOf("destination" to FoodStompDestinations.FOOD_ORDER_ASSIGNMENTS),
            body = "{invalid_json}"
        )

        every { mockBookingClient.subscribe(FoodStompDestinations.FOOD_ORDER_ASSIGNMENTS) } returns flowOf(badFrame)

        source.subscribeToFoodOrderAssignments().test {
            awaitComplete()
        }
    }
}
