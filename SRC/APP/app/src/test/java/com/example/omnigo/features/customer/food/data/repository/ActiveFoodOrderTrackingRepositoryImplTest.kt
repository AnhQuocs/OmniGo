package com.example.omnigo.features.customer.food.data.repository

import com.example.omnigo.features.customer.food.data.remote.dto.DriverLocationEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderAssignmentEventDto
import com.example.omnigo.features.customer.food.data.remote.dto.FoodOrderStatusEventDto
import com.example.omnigo.features.customer.food.data.remote.realtime.FoodStompSubscriptionSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveFoodOrderTrackingRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var repoScope: CoroutineScope

    private val mockStompSource = mockk<FoodStompSubscriptionSource>()
    private val statusFlow = MutableSharedFlow<FoodOrderStatusEventDto>()
    private val assignmentFlow = MutableSharedFlow<FoodOrderAssignmentEventDto>()
    private val locationFlow = MutableSharedFlow<DriverLocationEventDto>()

    private lateinit var repository: ActiveFoodOrderTrackingRepositoryImpl

    @Before
    fun setup() {
        repoScope = CoroutineScope(SupervisorJob() + testDispatcher)

        every { mockStompSource.subscribeToFoodOrderStatus(any()) } returns statusFlow
        every { mockStompSource.subscribeToFoodOrderAssignments() } returns assignmentFlow
        every { mockStompSource.subscribeToDriverLocation(any()) } returns locationFlow

        repository = ActiveFoodOrderTrackingRepositoryImpl(
            stompSource = mockStompSource,
            scope = repoScope
        )
    }

    @After
    fun tearDown() {
        repoScope.cancel()
    }

    @Test
    fun `activate initializes tracking state with given order id`() = testScope.runTest {
        repository.activate(101L)
        advanceUntilIdle()

        val state = repository.activeTrackingState.value
        assertNotNull(state)
        assertEquals(101L, state?.orderId)
        assertNull(state?.status)
        assertNull(state?.driverProfile)
        assertNull(state?.driverLocation)
    }

    @Test
    fun `receiving status event updates active tracking state status`() = testScope.runTest {
        repository.activate(101L)
        advanceUntilIdle()

        statusFlow.emit(FoodOrderStatusEventDto(orderId = 101L, status = "CONFIRMED"))
        advanceUntilIdle()

        val state = repository.activeTrackingState.value
        assertEquals("CONFIRMED", state?.status)
    }

    @Test
    fun `receiving assignment event updates driver profile and starts location tracking`() = testScope.runTest {
        repository.activate(101L)
        advanceUntilIdle()

        assignmentFlow.emit(
            FoodOrderAssignmentEventDto(
                orderId = 101L,
                driverId = 77L,
                driverName = "Nguyen Van Driver",
                driverPhone = "0912345678",
                vehiclePlate = "59-A1 999.99"
            )
        )
        advanceUntilIdle()

        val state = repository.activeTrackingState.value
        assertEquals(77L, state?.driverProfile?.driverId)
        assertEquals("Nguyen Van Driver", state?.driverProfile?.driverName)
        assertEquals("59-A1 999.99", state?.driverProfile?.vehiclePlate)

        // Location event should now update driver location
        locationFlow.emit(
            DriverLocationEventDto(
                driverId = 77L,
                latitude = 10.77,
                longitude = 106.70
            )
        )
        advanceUntilIdle()

        assertEquals(10.77, repository.activeTrackingState.value?.driverLocation?.latitude)
        assertEquals(106.70, repository.activeTrackingState.value?.driverLocation?.longitude)
    }

    @Test
    fun `clear resets state to null`() = testScope.runTest {
        repository.activate(101L)
        advanceUntilIdle()
        assertNotNull(repository.activeTrackingState.value)

        repository.clear()
        advanceUntilIdle()
        assertNull(repository.activeTrackingState.value)
    }

    @Test
    fun `receiving status event with mismatched orderId is ignored`() = testScope.runTest {
        repository.activate(101L)
        advanceUntilIdle()

        statusFlow.emit(FoodOrderStatusEventDto(orderId = 999L, status = "DELIVERED"))
        advanceUntilIdle()

        val state = repository.activeTrackingState.value
        assertNull(state?.status)
    }

    @Test
    fun `receiving out of order status event with older timestamp is ignored`() = testScope.runTest {
        repository.activate(101L)
        advanceUntilIdle()

        // First event with newer timestamp
        statusFlow.emit(
            FoodOrderStatusEventDto(
                orderId = 101L,
                status = "PREPARING",
                timestamp = "2026-10-22T10:00:00Z"
            )
        )
        advanceUntilIdle()
        assertEquals("PREPARING", repository.activeTrackingState.value?.status)

        // Delayed out-of-order event with older timestamp
        statusFlow.emit(
            FoodOrderStatusEventDto(
                orderId = 101L,
                status = "CONFIRMED",
                timestamp = "2026-10-22T09:50:00Z"
            )
        )
        advanceUntilIdle()
        // Status must remain PREPARING, not rolled back to CONFIRMED
        assertEquals("PREPARING", repository.activeTrackingState.value?.status)
    }

    @Test
    fun `receiving location event with mismatched driverId is ignored`() = testScope.runTest {
        repository.activate(101L)
        advanceUntilIdle()

        assignmentFlow.emit(
            FoodOrderAssignmentEventDto(
                orderId = 101L,
                driverId = 77L,
                driverName = "Nguyen Van Driver"
            )
        )
        advanceUntilIdle()

        // Location for a different driver
        locationFlow.emit(
            DriverLocationEventDto(
                driverId = 88L,
                latitude = 10.77,
                longitude = 106.70
            )
        )
        advanceUntilIdle()

        assertNull(repository.activeTrackingState.value?.driverLocation)
    }

    @Test
    fun `receiving status event with malformed timestamp does not crash and updates status`() = testScope.runTest {
        repository.activate(101L)
        advanceUntilIdle()

        statusFlow.emit(
            FoodOrderStatusEventDto(
                orderId = 101L,
                status = "READY_FOR_PICKUP",
                timestamp = "invalid-date-format"
            )
        )
        advanceUntilIdle()

        assertEquals("READY_FOR_PICKUP", repository.activeTrackingState.value?.status)
    }
}
