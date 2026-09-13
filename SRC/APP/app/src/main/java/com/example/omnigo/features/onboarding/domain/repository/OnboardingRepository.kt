package com.example.omnigo.features.onboarding.domain.repository

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val isOnboardingDoneFlow: Flow<Boolean>
    suspend fun isOnboardingDone(): Boolean
    suspend fun setOnboardingDone(isDone: Boolean)
}
