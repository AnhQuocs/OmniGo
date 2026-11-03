package com.example.omnigo.features.customer.profile.presentation.viewmodel

import app.cash.turbine.test
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.auth.domain.usecase.ChangePasswordUseCase
import com.example.omnigo.features.auth.domain.usecase.DeleteAccountUseCase
import com.example.omnigo.features.auth.domain.usecase.GetMeUseCase
import com.example.omnigo.features.auth.domain.usecase.LogoutUseCase
import com.example.omnigo.features.auth.domain.usecase.UpdateUserProfileUseCase
import com.example.omnigo.features.customer.address.domain.usecase.DeleteAddressUseCase
import com.example.omnigo.features.customer.address.domain.usecase.GetSavedAddressesUseCase
import com.example.omnigo.features.customer.address.domain.usecase.SaveAddressUseCase
import com.example.omnigo.features.customer.address.domain.usecase.UpdateSavedAddressUseCase
import com.example.omnigo.features.customer.ride.domain.model.GetDriverBookingsResult
import com.example.omnigo.features.driver.domain.usecase.GetDriverBookingsUseCase
import com.example.omnigo.features.driver.domain.usecase.GetDriverProfileUseCase
import com.example.omnigo.features.driver.domain.usecase.ResubmitDriverProfileUseCase
import com.example.omnigo.features.driver.domain.usecase.UpdateDriverVehicleUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var sessionManager: SessionManager
    private lateinit var getMeUseCase: GetMeUseCase
    private lateinit var avatarHelper: AccountAvatarHelper
    private lateinit var changePasswordUseCase: ChangePasswordUseCase
    private lateinit var updateUserProfileUseCase: UpdateUserProfileUseCase
    private lateinit var deleteAccountUseCase: DeleteAccountUseCase
    private lateinit var getSavedAddressesUseCase: GetSavedAddressesUseCase
    private lateinit var saveAddressUseCase: SaveAddressUseCase
    private lateinit var updateSavedAddressUseCase: UpdateSavedAddressUseCase
    private lateinit var deleteAddressUseCase: DeleteAddressUseCase
    private lateinit var getDriverProfileUseCase: GetDriverProfileUseCase
    private lateinit var getDriverBookingsUseCase: GetDriverBookingsUseCase
    private lateinit var updateDriverVehicleUseCase: UpdateDriverVehicleUseCase
    private lateinit var resubmitDriverProfileUseCase: ResubmitDriverProfileUseCase
    private lateinit var viewModel: AccountViewModel

    @Before
    fun setUp() {
        logoutUseCase = mockk(relaxed = true)
        sessionManager = mockk(relaxed = true)
        getMeUseCase = mockk { coEvery { this@mockk.invoke() } returns emptyFlow() }
        avatarHelper = mockk(relaxed = true)
        changePasswordUseCase = mockk { coEvery { this@mockk(any(), any(), any()) } returns Result.success("Success") }
        updateUserProfileUseCase = mockk { coEvery { this@mockk(any(), any()) } returns Result.failure(Exception("None")) }
        deleteAccountUseCase = mockk { coEvery { this@mockk() } returns Result.success("Success") }
        getSavedAddressesUseCase = mockk { coEvery { this@mockk() } returns Result.success(emptyList()) }
        saveAddressUseCase = mockk { coEvery { this@mockk(any(), any(), any(), any(), any()) } returns Result.success(mockk()) }
        updateSavedAddressUseCase = mockk { coEvery { this@mockk(any(), any(), any(), any(), any()) } returns Result.success(mockk()) }
        deleteAddressUseCase = mockk { coEvery { this@mockk(any()) } returns Result.success(Unit) }
        getDriverProfileUseCase = mockk { coEvery { this@mockk(any()) } returns Result.failure(Exception("None")) }
        getDriverBookingsUseCase = mockk { coEvery { this@mockk() } returns GetDriverBookingsResult.Success(emptyList()) }
        updateDriverVehicleUseCase = mockk { coEvery { this@mockk(any(), any(), any(), any()) } returns Result.failure(Exception("None")) }
        resubmitDriverProfileUseCase = mockk { coEvery { this@mockk(any(), any(), any(), any(), any(), any(), any()) } returns Result.failure(Exception("None")) }

        coEvery { sessionManager.getFullName() } returns "Bùi Anh Quốc"
        coEvery { sessionManager.getPhoneNumber() } returns "0839072300"
        coEvery { sessionManager.getRole() } returns "CUSTOMER"
        coEvery { sessionManager.getAvatarUrl() } returns null
        coEvery { sessionManager.getUserId() } returns 101L
    }

    private fun createViewModel(): AccountViewModel {
        return AccountViewModel(
            logoutUseCase = logoutUseCase,
            sessionManager = sessionManager,
            getMeUseCase = getMeUseCase,
            avatarHelper = avatarHelper,
            changePasswordUseCase = changePasswordUseCase,
            updateUserProfileUseCase = updateUserProfileUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            getSavedAddressesUseCase = getSavedAddressesUseCase,
            saveAddressUseCase = saveAddressUseCase,
            updateSavedAddressUseCase = updateSavedAddressUseCase,
            deleteAddressUseCase = deleteAddressUseCase,
            getDriverProfileUseCase = getDriverProfileUseCase,
            getDriverBookingsUseCase = getDriverBookingsUseCase,
            updateDriverVehicleUseCase = updateDriverVehicleUseCase,
            resubmitDriverProfileUseCase = resubmitDriverProfileUseCase
        )
    }

    @Test
    fun `init loads user profile from SessionManager successfully`() = runTest {
        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals("Bùi Anh Quốc", state.userName)
        assertEquals("0839072300", state.userPhone)
        assertEquals("CUSTOMER", state.userRole)
    }

    @Test
    fun `onLogoutClicked updates showLogoutDialog to true`() = runTest {
        viewModel = createViewModel()

        viewModel.onLogoutClicked()

        assertTrue(viewModel.uiState.value.showLogoutDialog)
    }

    @Test
    fun `onDismissLogoutDialog updates showLogoutDialog to false`() = runTest {
        viewModel = createViewModel()

        viewModel.onLogoutClicked()
        assertTrue(viewModel.uiState.value.showLogoutDialog)

        viewModel.onDismissLogoutDialog()
        assertFalse(viewModel.uiState.value.showLogoutDialog)
    }

    @Test
    fun `onConfirmLogout executes logout and sends NavigateToLogin event`() = runTest {
        viewModel = createViewModel()

        viewModel.uiEvent.test {
            viewModel.onConfirmLogout()

            coVerify(exactly = 1) { logoutUseCase.invoke() }
            assertEquals(AccountUiEvent.NavigateToLogin, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
