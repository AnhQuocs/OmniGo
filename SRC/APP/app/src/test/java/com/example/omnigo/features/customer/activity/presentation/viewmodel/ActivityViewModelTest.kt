package com.example.omnigo.features.customer.activity.presentation.viewmodel

import com.example.omnigo.features.customer.food.domain.model.FoodOrder
import com.example.omnigo.features.customer.food.domain.model.GetMenuItemsResult
import com.example.omnigo.features.customer.food.domain.model.GetMyFoodOrdersResult
import com.example.omnigo.features.customer.food.domain.model.GetRestaurantDetailResult
import com.example.omnigo.features.customer.food.domain.model.PaymentMethod
import com.example.omnigo.features.customer.food.domain.model.Restaurant
import com.example.omnigo.features.customer.food.domain.usecase.GetMyFoodOrdersUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantDetailUseCase
import com.example.omnigo.features.customer.food.domain.usecase.GetRestaurantItemsUseCase
import com.example.omnigo.features.customer.ride.data.local.RideTrackingSessionManager
import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.ActiveRideState
import com.example.omnigo.features.customer.ride.domain.model.Booking
import com.example.omnigo.features.customer.ride.domain.model.BookingDriverProfile
import com.example.omnigo.features.customer.ride.domain.model.BookingStatus
import com.example.omnigo.features.customer.ride.domain.model.CancelRideBookingResult
import com.example.omnigo.features.customer.ride.domain.model.GetBookingDriverResult
import com.example.omnigo.features.customer.ride.domain.model.GetCustomerBookingsResult
import com.example.omnigo.features.customer.ride.domain.usecase.CancelRideBookingUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.GetBookingDriverUseCase
import com.example.omnigo.features.customer.ride.domain.usecase.GetCustomerBookingsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getCustomerBookingsUseCase: GetCustomerBookingsUseCase
    private lateinit var getBookingDriverUseCase: GetBookingDriverUseCase
    private lateinit var cancelRideBookingUseCase: CancelRideBookingUseCase
    private lateinit var rideTrackingSessionManager: RideTrackingSessionManager
    private lateinit var getMyFoodOrdersUseCase: GetMyFoodOrdersUseCase
    private lateinit var getRestaurantDetailUseCase: GetRestaurantDetailUseCase
    private lateinit var getRestaurantItemsUseCase: GetRestaurantItemsUseCase
    private val activeRideFlow = MutableStateFlow<ActiveRideState?>(null)

    private val sampleBookings = listOf(
        Booking(
            bookingId = 1L,
            customerId = 5L,
            driverId = 99L,
            status = BookingStatus.ACCEPTED,
            startLatitude = 21.0,
            startLongitude = 105.0,
            endLatitude = 21.1,
            endLongitude = 105.1
        ),
        Booking(
            bookingId = 2L,
            customerId = 5L,
            status = BookingStatus.IN_PROGRESS,
            startLatitude = 21.0,
            startLongitude = 105.0,
            endLatitude = 21.1,
            endLongitude = 105.1
        ),
        Booking(
            bookingId = 3L,
            customerId = 5L,
            status = BookingStatus.COMPLETED,
            startLatitude = 21.0,
            startLongitude = 105.0,
            endLatitude = 21.1,
            endLongitude = 105.1
        ),
        Booking(
            bookingId = 4L,
            customerId = 5L,
            status = BookingStatus.CANCELLED,
            startLatitude = 21.0,
            startLongitude = 105.0,
            endLatitude = 21.1,
            endLongitude = 105.1
        )
    )

    private val sampleFoodOrders = listOf(
        FoodOrder(
            id = 10L,
            customerId = 5L,
            driverId = null,
            restaurantId = 50L,
            restaurantName = "Phở Thìn",
            status = "PREPARING",
            paymentMethod = PaymentMethod.CASH,
            isPaid = false,
            itemsPrice = 95000.0,
            deliveryFee = 0.0,
            totalPrice = 95000.0,
            dropOffAddress = "219 Cầu Giấy",
            dropOffLatitude = 21.03,
            dropOffLongitude = 105.79,
            note = "",
            items = emptyList(),
            estimatedDeliveryMinutes = 25,
            createdAt = "2026-10-20T09:30:00Z"
        ),
        FoodOrder(
            id = 11L,
            customerId = 5L,
            driverId = 88L,
            restaurantId = 50L,
            restaurantName = "Phở Thìn",
            status = "DELIVERED",
            paymentMethod = PaymentMethod.WALLET,
            isPaid = true,
            itemsPrice = 78000.0,
            deliveryFee = 0.0,
            totalPrice = 78000.0,
            dropOffAddress = "219 Cầu Giấy",
            dropOffLatitude = 21.03,
            dropOffLongitude = 105.79,
            note = "",
            items = emptyList(),
            estimatedDeliveryMinutes = 20,
            createdAt = "2026-10-18T18:45:00Z"
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getCustomerBookingsUseCase = mockk()
        getBookingDriverUseCase = mockk()
        cancelRideBookingUseCase = mockk()
        rideTrackingSessionManager = mockk(relaxed = true)
        getMyFoodOrdersUseCase = mockk()
        getRestaurantDetailUseCase = mockk()
        getRestaurantItemsUseCase = mockk()

        every { rideTrackingSessionManager.activeRideState } returns activeRideFlow
        coEvery { getBookingDriverUseCase(any()) } returns GetBookingDriverResult.Success(
            BookingDriverProfile(
                driverId = 99L,
                driverName = "Nguyễn Văn Minh",
                driverPhone = "0901234567",
                vehiclePlate = "29A-123.45",
                vehicleType = "BIKE",
                vehicleModel = "Honda Air Blade",
                avatarUrl = null
            )
        )
        coEvery { getRestaurantDetailUseCase(any()) } returns GetRestaurantDetailResult.Success(
            Restaurant(
                id = 50L,
                name = "Phở Thìn",
                phone = "0901234567",
                address = "Lò Đúc, Hai Bà Trưng, Hà Nội",
                latitude = 21.01,
                longitude = 105.85,
                imageUrl = "https://img/pho.jpg",
                status = "ACTIVE",
                openTime = "06:00",
                closeTime = "22:00",
                rating = 4.8,
                reviewCount = 120,
                isOpen = true,
                menuItems = emptyList()
            )
        )
        coEvery { getRestaurantItemsUseCase(any(), any()) } returns GetMenuItemsResult.Success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ActivityViewModel {
        return ActivityViewModel(
            getCustomerBookingsUseCase = getCustomerBookingsUseCase,
            getBookingDriverUseCase = getBookingDriverUseCase,
            cancelRideBookingUseCase = cancelRideBookingUseCase,
            rideTrackingSessionManager = rideTrackingSessionManager,
            getMyFoodOrdersUseCase = getMyFoodOrdersUseCase,
            getRestaurantDetailUseCase = getRestaurantDetailUseCase,
            getRestaurantItemsUseCase = getRestaurantItemsUseCase
        )
    }

    @Test
    fun `init loads both bookings and food orders and calculates ongoingCount`() = runTest {
        coEvery { getCustomerBookingsUseCase() } returns GetCustomerBookingsResult.Success(sampleBookings)
        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Success(sampleFoodOrders)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.ongoingBookings.size)
        assertEquals(2, state.historyBookings.size)
        assertEquals(1, state.ongoingFoodOrders.size)
        assertEquals(1, state.historyFoodOrders.size)
        assertEquals(3, state.ongoingCount)
        assertEquals("Nguyễn Văn Minh", state.driverProfiles[1L]?.driverName)
        assertEquals("https://img/pho.jpg", state.ongoingFoodOrders[0].restaurantImageUrl)
        assertTrue(state.hasOngoing)
        assertTrue(state.hasHistory)
    }

    @Test
    fun `loadActivities updates state with error message when usecase fails`() = runTest {
        coEvery {
            getCustomerBookingsUseCase()
        } returns GetCustomerBookingsResult.Error(
            error = RideError.NETWORK_ERROR,
            message = "Unable to connect"
        )
        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Success(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.ongoingBookings.isEmpty())
        assertTrue(state.historyBookings.isEmpty())
        assertEquals("Unable to connect", state.errorMessage)
    }

    @Test
    fun `onTabSelected and onHistoryServiceSelected update state accurately`() = runTest {
        coEvery { getCustomerBookingsUseCase() } returns GetCustomerBookingsResult.Success(sampleBookings)
        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Success(sampleFoodOrders)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onTabSelected(ActivityTab.HISTORY)
        viewModel.onHistoryServiceSelected(HistoryServiceFilter.OMNI_RIDE)

        val state = viewModel.uiState.value
        assertEquals(ActivityTab.HISTORY, state.selectedTab)
        assertEquals(HistoryServiceFilter.OMNI_RIDE, state.selectedHistoryService)
    }

    @Test
    fun `confirmCancelBooking invokes cancelRideBookingUseCase and reloads activities`() = runTest {
        coEvery { getCustomerBookingsUseCase() } returns GetCustomerBookingsResult.Success(sampleBookings)
        coEvery { getMyFoodOrdersUseCase() } returns GetMyFoodOrdersResult.Success(sampleFoodOrders)
        coEvery { cancelRideBookingUseCase(1L) } returns CancelRideBookingResult.Success(
            sampleBookings[0].copy(status = BookingStatus.CANCELLED)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.promptCancelBooking(1L)
        assertEquals(1L, viewModel.uiState.value.cancelConfirmBookingId)

        viewModel.confirmCancelBooking()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.cancelConfirmBookingId)
        assertFalse(viewModel.uiState.value.isCancellingBooking)
        coVerify(exactly = 1) { cancelRideBookingUseCase(1L) }
    }
}
