package com.example.omnigo.features.main.presentation.viewmodel

import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus
import com.example.omnigo.features.driver.food.domain.usecase.GetDriverActiveFoodOrdersUseCase
import com.example.omnigo.features.driver.ride.data.local.DriverTripSessionManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverMainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var driverTripSessionManager: DriverTripSessionManager
    private lateinit var getDriverActiveFoodOrdersUseCase: GetDriverActiveFoodOrdersUseCase

    private val activeBookingFlow = MutableStateFlow<Booking?>(null)

    private val sampleBooking = Booking(
        bookingId = 101L,
        customerId = 1L,
        driverId = 2L,
        status = BookingStatus.IN_PROGRESS,
        startLatitude = 21.0,
        startLongitude = 105.0,
        endLatitude = 21.1,
        endLongitude = 105.1
    )

    private val sampleFoodOrder = DriverDeliveryOrder(
        orderId = 202L,
        status = DriverDeliveryStatus.DELIVERING,
        restaurantName = "Test Kitchen",
        restaurantAddress = "Address 1",
        restaurantLatitude = 21.0,
        restaurantLongitude = 105.0,
        customerName = "Customer A",
        customerPhone = "0987654321",
        dropOffAddress = "Address 2",
        dropOffLatitude = 21.1,
        dropOffLongitude = 105.1,
        items = emptyList(),
        totalPrice = 50000.0,
        deliveryFee = 15000.0
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        driverTripSessionManager = mockk()
        getDriverActiveFoodOrdersUseCase = mockk()
        every { driverTripSessionManager.activeBooking } returns activeBookingFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `hasActiveTask is false initially when no active booking and no active food orders`() = runTest {
        coEvery { getDriverActiveFoodOrdersUseCase() } returns Result.success(emptyList())

        val viewModel = DriverMainViewModel(driverTripSessionManager, getDriverActiveFoodOrdersUseCase)
        val collectJob = launch { viewModel.hasActiveTask.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.hasActiveTask.value)
        collectJob.cancel()
    }

    @Test
    fun `hasActiveTask turns true when active booking is emitted`() = runTest {
        coEvery { getDriverActiveFoodOrdersUseCase() } returns Result.success(emptyList())

        val viewModel = DriverMainViewModel(driverTripSessionManager, getDriverActiveFoodOrdersUseCase)
        val collectJob = launch { viewModel.hasActiveTask.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.hasActiveTask.value)

        activeBookingFlow.value = sampleBooking
        advanceUntilIdle()

        assertTrue(viewModel.hasActiveTask.value)
        collectJob.cancel()
    }

    @Test
    fun `hasActiveTask turns true when active food delivery order exists`() = runTest {
        coEvery { getDriverActiveFoodOrdersUseCase() } returns Result.success(listOf(sampleFoodOrder))

        val viewModel = DriverMainViewModel(driverTripSessionManager, getDriverActiveFoodOrdersUseCase)
        val collectJob = launch { viewModel.hasActiveTask.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.hasActiveTask.value)
        collectJob.cancel()
    }
}

