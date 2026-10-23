package com.example.omnigo.features.driver.home.presentation.viewmodel

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.location.tracker.LocationTracker
import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.features.driver.domain.model.DriverFoodOffer
import com.example.omnigo.features.driver.domain.usecase.AcceptFoodOrderUseCase
import com.example.omnigo.features.driver.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.driver.domain.usecase.ObserveDriverFoodOffersUseCase
import com.example.omnigo.features.driver.domain.usecase.RejectFoodOrderUseCase
import com.example.omnigo.features.driver.domain.usecase.UpdateDriverStatusUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverHomeViewModelFoodOfferTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getDriverProfileUseCase: GetDriverProfileUseCase
    private lateinit var updateDriverStatusUseCase: UpdateDriverStatusUseCase
    private lateinit var locationTracker: LocationTracker
    private lateinit var sessionManager: SessionManager
    private lateinit var observeDriverFoodOffersUseCase: ObserveDriverFoodOffersUseCase
    private lateinit var acceptFoodOrderUseCase: AcceptFoodOrderUseCase
    private lateinit var rejectFoodOrderUseCase: RejectFoodOrderUseCase
    private lateinit var getDriverActiveFoodOrdersUseCase: com.example.omnigo.features.driver.food.domain.usecase.GetDriverActiveFoodOrdersUseCase

    private lateinit var offerFlow: MutableSharedFlow<DriverFoodOffer>

    private val onlineProfile = DriverAccountProfile(
        driverId = 101L,
        fullName = "Nguyen Van Driver",
        phoneNumber = "0912345678",
        vehiclePlate = "29A-12345",
        approvalStatus = DriverApprovalStatus.APPROVED,
        isOnline = true,
        avatarUrl = null
    )

    private val sampleOffer = DriverFoodOffer(
        orderId = 555L,
        restaurantName = "Com Tam Cali",
        restaurantAddress = "32 Nguyen Trai",
        dropOffAddress = "100 Le Loi",
        deliveryFee = 22000.0,
        totalPrice = 110000.0,
        isPaid = true,
        timeoutSeconds = 20
    )

    @Before
    fun setUp() {
        offerFlow = MutableSharedFlow(extraBufferCapacity = 64)
        getDriverProfileUseCase = mockk()
        updateDriverStatusUseCase = mockk()
        locationTracker = mockk()
        sessionManager = mockk()
        observeDriverFoodOffersUseCase = mockk()
        acceptFoodOrderUseCase = mockk()
        rejectFoodOrderUseCase = mockk()
        getDriverActiveFoodOrdersUseCase = mockk()

        coEvery { sessionManager.getUserId() } returns 101L
        coEvery { getDriverProfileUseCase(101L) } returns Result.success(onlineProfile)
        every { locationTracker.isLocationPermissionGranted() } returns true
        every { locationTracker.isGpsEnabled() } returns true
        every { observeDriverFoodOffersUseCase() } returns offerFlow
        coEvery { updateDriverStatusUseCase(any(), any()) } returns Result.success(true)
        coEvery { acceptFoodOrderUseCase(any()) } returns Result.success(true)
        coEvery { rejectFoodOrderUseCase(any()) } returns Result.success(true)
        coEvery { getDriverActiveFoodOrdersUseCase() } returns Result.success(emptyList())
    }

    private fun createViewModel(): DriverHomeViewModel {
        val vm = DriverHomeViewModel(
            getDriverProfileUseCase = getDriverProfileUseCase,
            updateDriverStatusUseCase = updateDriverStatusUseCase,
            locationTracker = locationTracker,
            sessionManager = sessionManager,
            observeDriverFoodOffersUseCase = observeDriverFoodOffersUseCase,
            acceptFoodOrderUseCase = acceptFoodOrderUseCase,
            rejectFoodOrderUseCase = rejectFoodOrderUseCase,
            getDriverActiveFoodOrdersUseCase = getDriverActiveFoodOrdersUseCase,
            rideOfferCoordinator = mockk(relaxed = true)
        )
        vm.timeProvider = { mainDispatcherRule.testDispatcher.scheduler.currentTime }
        return vm
    }

    @Test
    fun `incoming offer sets currentFoodOffer and initializes countdown when online`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOnline)

        offerFlow.emit(sampleOffer)
        runCurrent()

        val state = viewModel.uiState.value
        assertNotNull(state.currentFoodOffer)
        assertEquals(555L, state.currentFoodOffer?.orderId)
        assertEquals("Com Tam Cali", state.currentFoodOffer?.restaurantName)
        assertEquals(20, state.offerCountdownSeconds)
        assertEquals(1f, state.offerCountdownProgress, 0.01f)
    }

    @Test
    fun `incoming offer is ignored when driver is offline`() = runTest {
        val offlineProfile = onlineProfile.copy(isOnline = false)
        coEvery { getDriverProfileUseCase(101L) } returns Result.success(offlineProfile)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isOnline)

        viewModel.handleIncomingOffer(sampleOffer)
        runCurrent()

        assertNull(viewModel.uiState.value.currentFoodOffer)
    }

    @Test
    fun `duplicate offer with same orderId does not recreate offer`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        offerFlow.emit(sampleOffer)
        runCurrent()
        advanceTimeBy(3000L)
        runCurrent()

        val remainingSec = viewModel.uiState.value.offerCountdownSeconds

        // Emit same offer again
        offerFlow.emit(sampleOffer)
        runCurrent()

        // Countdown should continue without being reset to 20
        assertEquals(remainingSec, viewModel.uiState.value.offerCountdownSeconds)
    }

    @Test
    fun `new offer with different orderId replaces old offer and resets countdown`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        offerFlow.emit(sampleOffer)
        runCurrent()
        advanceTimeBy(4000L)
        runCurrent()

        val newOffer = sampleOffer.copy(orderId = 999L, restaurantName = "Banh Mi Huynh Hoa")
        offerFlow.emit(newOffer)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(999L, state.currentFoodOffer?.orderId)
        assertEquals("Banh Mi Huynh Hoa", state.currentFoodOffer?.restaurantName)
        assertEquals(20, state.offerCountdownSeconds)
    }

    @Test
    fun `countdown ticks down and triggers auto-reject when 20s expires`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        offerFlow.emit(sampleOffer)
        runCurrent()

        // Advance 21 seconds
        advanceTimeBy(21_000L)
        advanceUntilIdle()

        coVerify(exactly = 1) { rejectFoodOrderUseCase(555L) }
        val state = viewModel.uiState.value
        assertNull(state.currentFoodOffer)
        assertEquals(DriverHomeViewModel.TIMEOUT_MESSAGE, state.offerActionMessage)
    }

    @Test
    fun `manual reject calls rejectFoodOrderUseCase and dismisses offer dialog`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        offerFlow.emit(sampleOffer)
        runCurrent()

        viewModel.onRejectOffer()
        advanceUntilIdle()

        coVerify(exactly = 1) { rejectFoodOrderUseCase(555L) }
        val state = viewModel.uiState.value
        assertNull(state.currentFoodOffer)
        assertFalse(state.isOfferRejecting)
    }

    @Test
    fun `manual accept calls acceptFoodOrderUseCase and sets acceptedOrderId for navigation`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        offerFlow.emit(sampleOffer)
        runCurrent()

        viewModel.onAcceptOffer()
        advanceUntilIdle()

        coVerify(exactly = 1) { acceptFoodOrderUseCase(555L) }
        val state = viewModel.uiState.value
        assertNull(state.currentFoodOffer)
        assertFalse(state.isOfferAccepting)
        assertEquals(555L, state.acceptedOrderId)

        viewModel.clearAcceptedOrderId()
        assertNull(viewModel.uiState.value.acceptedOrderId)
    }

    @Test
    fun `accept failure due to conflict does not set acceptedOrderId and displays error`() = runTest {
        coEvery { acceptFoodOrderUseCase(555L) } returns Result.failure(RuntimeException("Order already accepted by another driver"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        offerFlow.emit(sampleOffer)
        runCurrent()

        viewModel.onAcceptOffer()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.currentFoodOffer)
        assertNull(state.acceptedOrderId)
        assertEquals("Order already accepted by another driver", state.errorMessage)
    }

    @Test
    fun `concurrent manual reject and timeout does not call reject twice`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        offerFlow.emit(sampleOffer)
        runCurrent()

        // Advance right to the edge and trigger manual reject
        advanceTimeBy(19_950L)
        runCurrent()
        viewModel.onRejectOffer()
        advanceTimeBy(200L)
        advanceUntilIdle()

        coVerify(exactly = 1) { rejectFoodOrderUseCase(555L) }
    }

    @Test
    fun `toggling status to offline cancels active offer and countdown`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        offerFlow.emit(sampleOffer)
        runCurrent()

        assertNotNull(viewModel.uiState.value.currentFoodOffer)

        viewModel.toggleOnlineStatus(false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isOnline)
        assertNull(state.currentFoodOffer)
    }

    @Test
    fun `incoming offer is ignored when orderId is zero or negative`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val invalidOffer = sampleOffer.copy(orderId = 0L)
        viewModel.handleIncomingOffer(invalidOffer)
        runCurrent()

        assertNull(viewModel.uiState.value.currentFoodOffer)
    }

    @Test
    fun `incoming offer does not overwrite current offer while accept is in flight`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        offerFlow.emit(sampleOffer)
        runCurrent()

        // Mock acceptUseCase to suspend
        var continueAccept: (() -> Unit)? = null
        coEvery { acceptFoodOrderUseCase(555L) } coAnswers {
            kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                continueAccept = { continuation.resume(Result.success(true)) {} }
            }
        }

        viewModel.onAcceptOffer()
        runCurrent()

        assertTrue(viewModel.uiState.value.isOfferAccepting)

        // New offer arrives while accepting 555L
        val competingOffer = sampleOffer.copy(orderId = 777L, restaurantName = "Pho 24")
        offerFlow.emit(competingOffer)
        runCurrent()

        // Should NOT overwrite 555L with 777L
        assertEquals(555L, viewModel.uiState.value.currentFoodOffer?.orderId)
        assertTrue(viewModel.uiState.value.isOfferAccepting)

        // Complete accept
        continueAccept?.invoke()
        advanceUntilIdle()

        assertEquals(555L, viewModel.uiState.value.acceptedOrderId)
    }
}
