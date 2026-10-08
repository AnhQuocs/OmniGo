package com.example.omnigo.features.driver.home.presentation.viewmodel

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.core.location.tracker.LocationTracker
import com.example.omnigo.features.driver.domain.model.DriverAccountProfile
import com.example.omnigo.features.driver.domain.model.DriverApprovalStatus
import com.example.omnigo.features.driver.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.driver.domain.usecase.UpdateDriverStatusUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getDriverProfileUseCase: GetDriverProfileUseCase
    private lateinit var updateDriverStatusUseCase: UpdateDriverStatusUseCase
    private lateinit var locationTracker: LocationTracker
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: DriverHomeViewModel

    private val approvedProfile = DriverAccountProfile(
        driverId = 101L,
        fullName = "Nguyen Van Driver",
        phoneNumber = "0912345678",
        vehiclePlate = "29A-12345",
        approvalStatus = DriverApprovalStatus.APPROVED,
        isOnline = false,
        avatarUrl = null
    )

    @Before
    fun setUp() {
        getDriverProfileUseCase = mockk()
        updateDriverStatusUseCase = mockk()
        locationTracker = mockk()
        sessionManager = mockk()

        coEvery { sessionManager.getUserId() } returns 101L
        coEvery { getDriverProfileUseCase(101L) } returns Result.success(approvedProfile)
        every { locationTracker.isLocationPermissionGranted() } returns true
        every { locationTracker.isGpsEnabled() } returns true
    }

    private fun createViewModel(): DriverHomeViewModel {
        return DriverHomeViewModel(
            getDriverProfileUseCase = getDriverProfileUseCase,
            updateDriverStatusUseCase = updateDriverStatusUseCase,
            locationTracker = locationTracker,
            sessionManager = sessionManager
        )
    }

    @Test
    fun `init loads driver profile and location prerequisites`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(approvedProfile, state.profile)
        assertEquals(DriverApprovalStatus.APPROVED, state.approvalStatus)
        assertFalse(state.isOnline)
        assertTrue(state.isLocationPermissionGranted)
        assertTrue(state.isGpsEnabled)
    }

    @Test
    fun `toggleOnlineStatus to true succeeds when profile is approved and location prerequisites met`() = runTest {
        coEvery { updateDriverStatusUseCase(101L, true) } returns Result.success(true)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleOnlineStatus(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isOnline)
        assertFalse(state.isStatusUpdating)
        assertNull(state.errorMessage)
        coVerify(exactly = 1) { updateDriverStatusUseCase(101L, true) }
    }

    @Test
    fun `toggleOnlineStatus to true is blocked when profile is PENDING_APPROVAL`() = runTest {
        val pendingProfile = approvedProfile.copy(approvalStatus = DriverApprovalStatus.PENDING_APPROVAL)
        coEvery { getDriverProfileUseCase(101L) } returns Result.success(pendingProfile)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleOnlineStatus(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isOnline)
        assertTrue(state.showApprovalWarningDialog)
        coVerify(exactly = 0) { updateDriverStatusUseCase(any(), any()) }
    }

    @Test
    fun `toggleOnlineStatus to true is blocked when profile is INACTIVE`() = runTest {
        val inactiveProfile = approvedProfile.copy(approvalStatus = DriverApprovalStatus.INACTIVE)
        coEvery { getDriverProfileUseCase(101L) } returns Result.success(inactiveProfile)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleOnlineStatus(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isOnline)
        assertTrue(state.showApprovalWarningDialog)
        coVerify(exactly = 0) { updateDriverStatusUseCase(any(), any()) }
    }

    @Test
    fun `toggleOnlineStatus to true requires permission when location permission not granted`() = runTest {
        every { locationTracker.isLocationPermissionGranted() } returns false

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleOnlineStatus(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isOnline)
        assertEquals(DriverHomeViewModel.ERROR_LOCATION_PERMISSION_REQUIRED, state.errorMessage)
        coVerify(exactly = 0) { updateDriverStatusUseCase(any(), any()) }
    }

    @Test
    fun `toggleOnlineStatus to true requires GPS when GPS disabled`() = runTest {
        every { locationTracker.isGpsEnabled() } returns false

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleOnlineStatus(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isOnline)
        assertEquals(DriverHomeViewModel.ERROR_GPS_REQUIRED, state.errorMessage)
        coVerify(exactly = 0) { updateDriverStatusUseCase(any(), any()) }
    }

    @Test
    fun `toggleOnlineStatus to false turns offline even if permissions missing`() = runTest {
        coEvery { updateDriverStatusUseCase(101L, false) } returns Result.success(true)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleOnlineStatus(false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isOnline)
        assertFalse(state.isStatusUpdating)
        coVerify(exactly = 1) { updateDriverStatusUseCase(101L, false) }
    }

    @Test
    fun `dismissApprovalWarningDialog resets showApprovalWarningDialog to false`() = runTest {
        val pendingProfile = approvedProfile.copy(approvalStatus = DriverApprovalStatus.PENDING_APPROVAL)
        coEvery { getDriverProfileUseCase(101L) } returns Result.success(pendingProfile)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleOnlineStatus(true)
        assertTrue(viewModel.uiState.value.showApprovalWarningDialog)

        viewModel.dismissApprovalWarningDialog()
        assertFalse(viewModel.uiState.value.showApprovalWarningDialog)
    }

    @Test
    fun `clearErrorMessage resets errorMessage to null`() = runTest {
        every { locationTracker.isLocationPermissionGranted() } returns false
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.toggleOnlineStatus(true)
        assertEquals(DriverHomeViewModel.ERROR_LOCATION_PERMISSION_REQUIRED, viewModel.uiState.value.errorMessage)

        viewModel.clearErrorMessage()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
