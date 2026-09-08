package com.example.omnigo.features.profile.presentation.viewmodel

import app.cash.turbine.test
import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.auth.domain.model.AuthUser
import com.example.omnigo.features.auth.domain.model.UserRole
import com.example.omnigo.features.auth.domain.model.UserStatus
import com.example.omnigo.features.auth.domain.usecase.LogoutUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var viewModel: AccountViewModel

    @Before
    fun setUp() {
        logoutUseCase = mockk(relaxed = true)
        sessionManager = mockk()

        coEvery { sessionManager.getFullName() } returns "Bùi Anh Quốc"
        coEvery { sessionManager.getPhoneNumber() } returns "0839072300"
        coEvery { sessionManager.getRole() } returns "CUSTOMER"
    }

    @Test
    fun `init loads user profile from SessionManager successfully`() = runTest {
        viewModel = AccountViewModel(logoutUseCase, sessionManager)

        val state = viewModel.uiState.value
        assertEquals("Bùi Anh Quốc", state.userName)
        assertEquals("0839072300", state.userPhone)
        assertEquals("CUSTOMER", state.userRole)
    }

    @Test
    fun `onLogoutClicked updates showLogoutDialog to true`() = runTest {
        viewModel = AccountViewModel(logoutUseCase, sessionManager)

        viewModel.onLogoutClicked()

        assertTrue(viewModel.uiState.value.showLogoutDialog)
    }

    @Test
    fun `onDismissLogoutDialog updates showLogoutDialog to false`() = runTest {
        viewModel = AccountViewModel(logoutUseCase, sessionManager)

        viewModel.onLogoutClicked()
        assertTrue(viewModel.uiState.value.showLogoutDialog)

        viewModel.onDismissLogoutDialog()
        assertFalse(viewModel.uiState.value.showLogoutDialog)
    }

    @Test
    fun `onConfirmLogout executes logout and sends NavigateToLogin event`() = runTest {
        viewModel = AccountViewModel(logoutUseCase, sessionManager)

        viewModel.uiEvent.test {
            viewModel.onConfirmLogout()

            coVerify(exactly = 1) { logoutUseCase.invoke() }

            val event = awaitItem()
            assertTrue(event is AccountUiEvent.NavigateToLogin)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
