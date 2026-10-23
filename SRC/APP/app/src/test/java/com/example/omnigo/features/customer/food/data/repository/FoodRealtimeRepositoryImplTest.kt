package com.example.omnigo.features.customer.food.data.repository

import com.example.omnigo.features.customer.food.data.remote.dto.DriverLocationEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderAssignmentEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderStatusEventDto
import com.example.omnigo.features.customer.food.data.remote.realtime.FoodStompSubscriptionSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FoodRealtimeRepositoryImplTest {

    private lateinit var stompSource: FoodStompSubscriptionSource
    private lateinit var repository: FoodRealtimeRepositoryImpl

    @Before
    fun setUp() {
        stompSource = mockk()
        repository = FoodRealtimeRepositoryImpl(stompSource)
    }

    @Test
    fun `observeFoodOrderAssignments emits domain assignment event`() = runTest {
        val dto = FoodOrderAssignmentEventDto(
            orderId = 12L,
            driverId = 34L,
            driverName = "Tài xế Minh",
            driverPhone = "0900111222",
            vehiclePlate = "59-X1 12345"
        )
        every { stompSource.subscribeToFoodOrderAssignments() } returns flowOf(dto)

        val result = repository.observeFoodOrderAssignments().first()

        assertEquals(12L, result.orderId)
        assertEquals(34L, result.driverId)
        assertEquals("Tài xế Minh", result.driverName)
        assertEquals("0900111222", result.driverPhone)
        assertEquals("59-X1 12345", result.vehiclePlate)
    }

    @Test
    fun `observeFoodOrderStatus emits domain status event`() = runTest {
        val dto = FoodOrderStatusEventDto(
            orderId = 12L,
            status = "DELIVERING",
            timestamp = "2026-10-23T09:10:00Z"
        )
        every { stompSource.subscribeToFoodOrderStatus(12L) } returns flowOf(dto)

        val result = repository.observeFoodOrderStatus(12L).first()

        assertEquals(12L, result.orderId)
        assertEquals("DELIVERING", result.status)
        assertEquals("2026-10-23T09:10:00Z", result.timestamp)
    }

    @Test
    fun `observeDriverLocation emits domain driver location event`() = runTest {
        val dto = DriverLocationEventDto(
            driverId = 34L,
            latitude = 21.0285,
            longitude = 105.8542,
            bearing = 180.0,
            speed = 10.0,
            timestamp = "2026-10-23T09:15:00Z"
        )
        every { stompSource.subscribeToDriverLocation(34L) } returns flowOf(dto)

        val result = repository.observeDriverLocation(34L).first()

        assertEquals(34L, result.driverId)
        assertEquals(21.0285, result.latitude, 0.0001)
        assertEquals(105.8542, result.longitude, 0.0001)
        assertEquals(180.0, result.bearing!!, 0.0001)
        assertEquals(10.0, result.speed!!, 0.0001)
    }
}
