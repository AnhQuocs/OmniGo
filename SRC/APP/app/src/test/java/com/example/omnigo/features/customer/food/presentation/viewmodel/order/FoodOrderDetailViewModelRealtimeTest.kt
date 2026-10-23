package com.example.omnigo.features.customer.food.presentation.viewmodel.order

import androidx.lifecycle.SavedStateHandle
import com.example.omnigo.features.customer.food.domain.model.DriverLocation
import com.example.omnigo.features.customer.food.domain.model.DriverProfile
import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.FoodOrderTrackingState
import com.example.omnigo.features.customer.food.domain.model.GetFoodOrderDetailResult
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.repository.ActiveFoodOrderTrackingRepository
import com.example.omnigo.features.customer.food.domain.usecase.CancelFoodOrderUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetFoodOrderDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.RetryDriverUseCase
import com.example.omnigo.features.customer.food.domain.usecase.SwitchToCashUseCase
import com.example.omnigo.features.customer.food.presentation.viewmodel.FoodOrderDetailViewModel
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoodOrderDetailViewModelRealtimeTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getFoodOrderDetailUseCase: GetFoodOrderDetailUseCase
    private lateinit var getDriverProfileUseCase: GetDriverProfileUseCase
    private lateinit var cancelFoodOrderUseCase: CancelFoodOrderUseCase
    private lateinit var switchToCashUseCase: SwitchToCashUseCase
    private lateinit var retryDriverUseCase: RetryDriverUseCase
    private lateinit var activeTrackingRepository: ActiveFoodOrderTrackingRepository

    private val trackingStateFlow = MutableStateFlow<FoodOrderTrackingState?>(null)

    private val mockOrder = FoodOrder(
        id = 123L,
        customerId = 1L,
        driverId = null,
        driverName = null,
        driverPhone = null,
        driverVehiclePlate = null,
        restaurantId = 5L,
        restaurantName = "Phở Thìn Lò Đúc",
        restaurantAddress = "13 Lò Đúc, Hai Bà Trưng",
        restaurantLatitude = 21.0180,
        restaurantLongitude = 105.8560,
        status = "PREPARING",
        paymentMethod = PaymentMethod.WALLET,
        isPaid = true,
        itemsPrice = 65000.0,
        deliveryFee = 15000.0,
        totalPrice = 80000.0,
        dropOffAddress = "Hà Nội",
        dropOffLatitude = 21.0150,
        dropOffLongitude = 105.8520,
        note = "",
        items = emptyList(),
        estimatedDeliveryMinutes = 25,
        createdAt = "2026-10-23T08:00:00Z"
    )

    @Before
    fun setUp() {
        getFoodOrderDetailUseCase = mockk()
        getDriverProfileUseCase = mockk(relaxed = true)
        cancelFoodOrderUseCase = mockk()
        switchToCashUseCase = mockk()
        retryDriverUseCase = mockk()
        activeTrackingRepository = mockk(relaxed = true)

        every { activeTrackingRepository.activeTrackingState } returns trackingStateFlow
        coEvery { getFoodOrderDetailUseCase(123L) } returns GetFoodOrderDetailResult.Success(mockOrder)
    }

    private fun createViewModel(orderId: Long = 123L): FoodOrderDetailViewModel {
        return FoodOrderDetailViewModel(
            getFoodOrderDetailUseCase = getFoodOrderDetailUseCase,
            getDriverProfileUseCase = getDriverProfileUseCase,
            cancelFoodOrderUseCase = cancelFoodOrderUseCase,
            switchToCashUseCase = switchToCashUseCase,
            retryDriverUseCase = retryDriverUseCase,
            activeTrackingRepository = activeTrackingRepository,
            savedStateHandle = SavedStateHandle(mapOf("orderId" to orderId))
        )
    }

    @Test
    fun `init activates realtime tracking with orderId`() = runTest {
        val vm = createViewModel(123L)
        advanceUntilIdle()

        coVerify(exactly = 1) { activeTrackingRepository.activate(123L) }
    }

    @Test
    fun `realtime status event updates order status in uiState`() = runTest {
        val vm = createViewModel(123L)
        advanceUntilIdle()

        assertEquals("PREPARING", vm.uiState.value.order?.status)

        // Backend broadcasts PICKED_UP
        trackingStateFlow.value = FoodOrderTrackingState(
            orderId = 123L,
            status = "PICKED_UP",
            driverProfile = null,
            driverLocation = null
        )
        advanceUntilIdle()

        assertEquals("PICKED_UP", vm.uiState.value.order?.status)
    }

    @Test
    fun `realtime driver assignment event updates driver info in order and state`() = runTest {
        val vm = createViewModel(123L)
        advanceUntilIdle()

        assertNull(vm.uiState.value.order?.driverId)

        val profile = DriverProfile(
            driverId = 99L,
            driverName = "Nguyễn Văn Shipper",
            driverPhone = "0912345678",
            vehiclePlate = "29A1-12345",
            avatarUrl = null
        )

        trackingStateFlow.value = FoodOrderTrackingState(
            orderId = 123L,
            status = "DRIVER_ASSIGNED",
            driverProfile = profile,
            driverLocation = null
        )
        advanceUntilIdle()

        assertEquals(99L, vm.uiState.value.order?.driverId)
        assertEquals("Nguyễn Văn Shipper", vm.uiState.value.order?.driverName)
        assertEquals("0912345678", vm.uiState.value.order?.driverPhone)
        assertEquals(profile, vm.uiState.value.driverProfile)
    }

    @Test
    fun `realtime driver location event updates driverLocation in uiState`() = runTest {
        val vm = createViewModel(123L)
        advanceUntilIdle()

        assertNull(vm.uiState.value.driverLocation)

        val location = DriverLocation(
            driverId = 99L,
            latitude = 21.0165,
            longitude = 105.8540,
            bearing = 90.0,
            speed = 8.5,
            timestamp = "2026-10-23T08:15:00Z"
        )

        trackingStateFlow.value = FoodOrderTrackingState(
            orderId = 123L,
            status = "DELIVERING",
            driverProfile = null,
            driverLocation = location
        )
        advanceUntilIdle()

        val liveLoc = vm.uiState.value.driverLocation
        assertNotNull(liveLoc)
        assertEquals(21.0165, liveLoc!!.latitude, 0.0001)
        assertEquals(105.8540, liveLoc.longitude, 0.0001)
        assertEquals(90.0, liveLoc.bearing!!, 0.0001)
        assertEquals(8.5, liveLoc.speed!!, 0.0001)
    }

    @Test
    fun `realtime tracking ignores event for different orderId`() = runTest {
        val vm = createViewModel(123L)
        advanceUntilIdle()

        assertEquals("PREPARING", vm.uiState.value.order?.status)

        // Event from another order
        trackingStateFlow.value = FoodOrderTrackingState(
            orderId = 999L,
            status = "DELIVERED",
            driverProfile = null,
            driverLocation = null
        )
        advanceUntilIdle()

        // Status must NOT change
        assertEquals("PREPARING", vm.uiState.value.order?.status)
    }
}
