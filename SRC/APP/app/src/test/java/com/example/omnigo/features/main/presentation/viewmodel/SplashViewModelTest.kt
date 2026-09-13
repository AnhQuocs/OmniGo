package com.example.omnigo.features.main.presentation.viewmodel

import com.example.omnigo.core.datastore.SessionManager
import com.example.omnigo.features.onboarding.domain.usecase.IsOnboardingCompletedUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var isOnboardingCompletedUseCase: IsOnboardingCompletedUseCase
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: SplashViewModel

    @Before
    fun setUp() {
        isOnboardingCompletedUseCase = mockk()
        sessionManager = mockk()
        viewModel = SplashViewModel(isOnboardingCompletedUseCase, sessionManager)
    }

    @Test
    fun `decideStartDestination returns onboarding_root when onboarding is not completed`() = runTest {
        coEvery { isOnboardingCompletedUseCase() } returns false

        var destination: String? = null
        viewModel.decideStartDestination {
            destination = it
        }

        assertEquals("onboarding_root", destination)
    }

    @Test
    fun `decideStartDestination returns driver_root when user is driver and logged in`() = runTest {
        coEvery { isOnboardingCompletedUseCase() } returns true
        coEvery { sessionManager.getAccessToken() } returns "valid_jwt_token"
        coEvery { sessionManager.getRole() } returns "DRIVER"

        var destination: String? = null
        viewModel.decideStartDestination {
            destination = it
        }

        assertEquals("driver_root", destination)
    }

    @Test
    fun `decideStartDestination returns customer_root when user is customer and logged in`() = runTest {
        coEvery { isOnboardingCompletedUseCase() } returns true
        coEvery { sessionManager.getAccessToken() } returns "valid_jwt_token"
        coEvery { sessionManager.getRole() } returns "CUSTOMER"

        var destination: String? = null
        viewModel.decideStartDestination {
            destination = it
        }

        assertEquals("customer_root", destination)
    }

    @Test
    fun `decideStartDestination returns auth_root when onboarding is completed and user is not logged in`() = runTest {
        coEvery { isOnboardingCompletedUseCase() } returns true
        coEvery { sessionManager.getAccessToken() } returns null
        coEvery { sessionManager.getRole() } returns null

        var destination: String? = null
        viewModel.decideStartDestination {
            destination = it
        }

        assertEquals("auth_root", destination)
    }
}
