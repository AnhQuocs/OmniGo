package com.example.omnigo.features.onboarding.domain.usecase

import com.example.omnigo.features.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val onboardingRepository: OnboardingRepository
) {
    suspend operator fun invoke() {
        onboardingRepository.setOnboardingDone(true)
    }
}
