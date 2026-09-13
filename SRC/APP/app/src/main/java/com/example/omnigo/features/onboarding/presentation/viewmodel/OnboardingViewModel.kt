package com.example.omnigo.features.onboarding.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.onboarding.domain.usecase.CompleteOnboardingUseCase
import com.example.omnigo.features.onboarding.presentation.model.OnboardingPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    val pages: List<OnboardingPage> = OnboardingPage.getPages()

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            completeOnboardingUseCase()
            onSuccess()
        }
    }
}
