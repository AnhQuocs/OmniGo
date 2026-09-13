package com.example.omnigo.features.onboarding.presentation.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.omnigo.R
import kotlin.collections.listOf

data class OnboardingPage(
    @get:DrawableRes val imageRes: Int,
    @get:StringRes val titleRes: Int,
    @get:StringRes val descRes: Int
) {
    companion object {
        fun getPages(): List<OnboardingPage> = listOf(
            OnboardingPage(
                imageRes = R.drawable.img_onboarding_ride,
                titleRes = R.string.onboarding_title_1,
                descRes = R.string.onboarding_desc_1
            ),
            OnboardingPage(
                imageRes = R.drawable.img_onboarding_food,
                titleRes = R.string.onboarding_title_2,
                descRes = R.string.onboarding_desc_2
            ),
            OnboardingPage(
                imageRes = R.drawable.img_onboarding_payment,
                titleRes = R.string.onboarding_title_3,
                descRes = R.string.onboarding_desc_3
            )
        )
    }
}
