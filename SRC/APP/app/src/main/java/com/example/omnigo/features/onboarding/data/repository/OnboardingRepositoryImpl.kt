package com.example.omnigo.features.onboarding.data.repository

import com.example.omnigo.features.onboarding.data.local.OnboardingDataStore
import com.example.omnigo.features.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    private val onboardingDataStore: OnboardingDataStore
) : OnboardingRepository {

    override val isOnboardingDoneFlow: Flow<Boolean>
        get() = onboardingDataStore.isOnboardingDoneFlow

    override suspend fun isOnboardingDone(): Boolean {
        return onboardingDataStore.isOnboardingDone()
    }

    override suspend fun setOnboardingDone(isDone: Boolean) {
        onboardingDataStore.setOnboardingDone(isDone)
    }
}
