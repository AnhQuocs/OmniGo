package com.example.omnigo.features.driver.food.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.omnigo.core.location.model.UserLocation
import com.example.omnigo.core.location.tracker.LocationTracker
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryOrder
import com.example.omnigo.features.driver.food.domain.model.DriverDeliveryStatus
import com.example.omnigo.features.driver.food.domain.usecase.GetDriverFoodOrderDetailUseCase
import com.example.omnigo.features.driver.food.domain.usecase.UpdateDriverFoodOrderStatusUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverActiveDeliveryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getOrderDetailUseCase: GetDriverFoodOrderDetailUseCase
    private lateinit var updateStatusUseCase: UpdateDriverFoodOrderStatusUseCase
    private lateinit var locationTracker: LocationTracker

    private val testOrder = DriverDeliveryOrder(
        orderId = 100L,
        status = DriverDeliveryStatus.ACCEPTED,
        restaurantName = "Quan Pho Ngon",
        restaurantAddress = "123 Nguyen Trai",
        restaurantLatitude = 10.762622,
        restaurantLongitude = 106.660172,
        customerName = "Nguyen Van Khach",
        customerPhone = "0909123456",
        dropOffAddress = "456 Le Loi",
        dropOffLatitude = 10.772622,
        dropOffLongitude = 106.670172,
        items = emptyList(),
        totalPrice = 150000.0,
        deliveryFee = 25000.0,
        isPaid = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getOrderDetailUseCase = mockk()
        updateStatusUseCase = mockk()
        locationTracker = mockk()

        every { locationTracker.isLocationPermissionGranted() } returns true
        every { locationTracker.isGpsEnabled() } returns true
        coEvery { locationTracker.getCurrentLocation() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): DriverActiveDeliveryViewModel {
        return DriverActiveDeliveryViewModel(
            savedStateHandle = savedStateHandle,
            getDriverFoodOrderDetailUseCase = getOrderDetailUseCase,
            updateDriverFoodOrderStatusUseCase = updateStatusUseCase,
            locationTracker = locationTracker
        )
    }

    @Test
    fun `loadOrderDetail loads order detail successfully`() = runTest {
        coEvery { getOrderDetailUseCase(100L) } returns Result.success(testOrder)

        val viewModel = createViewModel()
        viewModel.loadOrderDetail(100L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.order)
        assertEquals(100L, state.order?.orderId)
        assertEquals(DriverDeliveryStatus.ACCEPTED, state.order?.status)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadOrderDetail falls back to savedStateHandle coordinates when dto coordinates are null`() = runTest {
        val orderWithoutCoords = testOrder.copy(restaurantLatitude = null, restaurantLongitude = null)
        coEvery { getOrderDetailUseCase(100L) } returns Result.success(orderWithoutCoords)

        val handle = SavedStateHandle(
            mapOf("restaurantLat" to "10.762622", "restaurantLng" to "106.660172")
        )
        val viewModel = createViewModel(handle)
        viewModel.loadOrderDetail(100L)
        advanceUntilIdle()

        val order = viewModel.uiState.value.order
        assertNotNull(order)
        assertEquals(10.762622, order?.restaurantLatitude ?: 0.0, 0.0001)
        assertEquals(106.660172, order?.restaurantLongitude ?: 0.0, 0.0001)

        viewModel.updateLocationAndDistance(10.762622, 106.660172)
        assertTrue(viewModel.uiState.value.isWithinGeofence)
    }

    @Test
    fun `loadOrderDetail sets error message when load fails`() = runTest {
        coEvery { getOrderDetailUseCase(100L) } returns Result.failure(RuntimeException("Order not found"))

        val viewModel = createViewModel()
        viewModel.loadOrderDetail(100L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.order)
        assertEquals("Order not found", state.errorMessage)
    }

    @Test
    fun `updateLocationAndDistance calculates geofence correctly for restaurant`() = runTest {
        coEvery { getOrderDetailUseCase(100L) } returns Result.success(testOrder)
        val viewModel = createViewModel()
        viewModel.loadOrderDetail(100L)
        advanceUntilIdle()

        // Gần nhà hàng (cùng tọa độ)
        viewModel.updateLocationAndDistance(10.762622, 106.660172)

        var state = viewModel.uiState.value
        assertTrue(state.isWithinGeofence)
        assertEquals(0.0, state.distanceToTargetMeters ?: 100.0, 1.0)

        // Xa nhà hàng (> 1000m)
        viewModel.updateLocationAndDistance(10.780000, 106.680000)

        state = viewModel.uiState.value
        assertFalse(state.isWithinGeofence)
        assertTrue((state.distanceToTargetMeters ?: 0.0) > 50.0)
    }

    @Test
    fun `onConfirmPickup fails when driver is outside geofence`() = runTest {
        coEvery { getOrderDetailUseCase(100L) } returns Result.success(testOrder)
        val viewModel = createViewModel()
        viewModel.loadOrderDetail(100L)
        advanceUntilIdle()

        // Driver ở xa nhà hàng
        viewModel.updateLocationAndDistance(10.800000, 106.700000)

        viewModel.onConfirmPickup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DriverActiveDeliveryViewModel.ERROR_GEOFENCE_OUTSIDE, state.errorMessage)
        coVerify(exactly = 0) { updateStatusUseCase(any(), any()) }
    }

    @Test
    fun `onConfirmPickup fails when GPS returns null even if previously in geofence`() = runTest {
        coEvery { getOrderDetailUseCase(100L) } returns Result.success(testOrder)
        val viewModel = createViewModel()
        viewModel.loadOrderDetail(100L)
        advanceUntilIdle()

        // Ban đầu đang trong geofence
        viewModel.updateLocationAndDistance(10.762622, 106.660172)
        assertTrue(viewModel.uiState.value.isWithinGeofence)

        // Sau đó GPS bị mất (trả về null)
        coEvery { locationTracker.getCurrentLocation() } returns null

        viewModel.onConfirmPickup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isWithinGeofence)
        assertNull(state.distanceToTargetMeters)
        assertEquals(DriverActiveDeliveryViewModel.ERROR_GEOFENCE_OUTSIDE, state.errorMessage)
        coVerify(exactly = 0) { updateStatusUseCase(any(), any()) }
    }

    @Test
    fun `onConfirmPickup calls updateStatusUseCase when within geofence and updates to DELIVERING`() = runTest {
        coEvery { getOrderDetailUseCase(100L) } returns Result.success(testOrder)
        val updatedOrder = testOrder.copy(status = DriverDeliveryStatus.DELIVERING)
        coEvery { updateStatusUseCase(100L, "DELIVERING") } returns Result.success(updatedOrder)

        val mockLocation = UserLocation(
            latitude = 10.762622,
            longitude = 106.660172
        )
        coEvery { locationTracker.getCurrentLocation() } returns mockLocation

        val viewModel = createViewModel()
        viewModel.loadOrderDetail(100L)
        advanceUntilIdle()

        viewModel.updateLocationAndDistance(10.762622, 106.660172)
        assertTrue(viewModel.uiState.value.isWithinGeofence)

        viewModel.onConfirmPickup()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isUpdatingStatus)
        assertEquals(DriverDeliveryStatus.DELIVERING, state.order?.status)
        coVerify(exactly = 1) { updateStatusUseCase(100L, "DELIVERING") }
    }

    @Test
    fun `onConfirmDelivered updates to COMPLETED and marks isCompleted`() = runTest {
        val deliveringOrder = testOrder.copy(status = DriverDeliveryStatus.DELIVERING)
        coEvery { getOrderDetailUseCase(100L) } returns Result.success(deliveringOrder)
        val completedOrder = deliveringOrder.copy(status = DriverDeliveryStatus.COMPLETED)
        coEvery { updateStatusUseCase(100L, "COMPLETED") } returns Result.success(completedOrder)

        val mockLocation = UserLocation(
            latitude = 10.772622,
            longitude = 106.670172
        )
        coEvery { locationTracker.getCurrentLocation() } returns mockLocation

        val viewModel = createViewModel()
        viewModel.loadOrderDetail(100L)
        advanceUntilIdle()

        viewModel.updateLocationAndDistance(10.772622, 106.670172)
        assertTrue(viewModel.uiState.value.isWithinGeofence)

        viewModel.onConfirmDelivered()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isUpdatingStatus)
        assertTrue(state.isCompleted)
        assertEquals(DriverDeliveryStatus.COMPLETED, state.order?.status)
        coVerify(exactly = 1) { updateStatusUseCase(100L, "COMPLETED") }
    }

    @Test
    fun `onConfirmPickup prevents re-entrancy while update is in-flight`() = runTest {
        coEvery { getOrderDetailUseCase(100L) } returns Result.success(testOrder)
        val deliveringOrder = testOrder.copy(status = DriverDeliveryStatus.DELIVERING)
        coEvery { updateStatusUseCase(100L, "DELIVERING") } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(deliveringOrder)
        }

        val mockLocation = UserLocation(
            latitude = 10.762622,
            longitude = 106.660172
        )
        coEvery { locationTracker.getCurrentLocation() } returns mockLocation

        val viewModel = createViewModel()
        viewModel.loadOrderDetail(100L)
        advanceUntilIdle()

        viewModel.updateLocationAndDistance(10.762622, 106.660172)

        viewModel.onConfirmPickup()
        viewModel.onConfirmPickup()

        advanceUntilIdle()

        coVerify(exactly = 1) { updateStatusUseCase(100L, "DELIVERING") }
    }
}
