package com.example.omnigo.features.onboarding.presentation.viewmodel

import com.example.omnigo.features.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.example.omnigo.utils.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var completeOnboardingUseCase: CompleteOnboardingUseCase
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        completeOnboardingUseCase = mockk(relaxed = true)
        viewModel = OnboardingViewModel(completeOnboardingUseCase)
    }

    @Test
    fun `pages property contains exactly 3 onboarding pages`() {
        assertEquals(3, viewModel.pages.size)
    }

    @Test
    fun `completeOnboarding executes CompleteOnboardingUseCase and invokes onSuccess callback`() = runTest {
        var isSuccessCalled = false

        viewModel.completeOnboarding {
            isSuccessCalled = true
        }

        coVerify(exactly = 1) { completeOnboardingUseCase.invoke() }
        assertTrue(isSuccessCalled)
    }
}
