package com.example.omnigo.features.language.domain.usecase

import com.example.omnigo.features.language.data.preference.LanguagePreferenceManager
import com.example.omnigo.features.language.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class LanguageUseCases @Inject constructor(
    val getLanguageUseCase: GetLanguageUseCase,
    val updateLanguageUseCase: UpdateLanguageUseCase
)

class GetLanguageUseCase @Inject constructor(
    private val manager: LanguagePreferenceManager
) {
    operator fun invoke(): Flow<AppLanguage> = manager.languageFlow
}

class UpdateLanguageUseCase @Inject constructor(
    private val manager: LanguagePreferenceManager
) {
    suspend operator fun invoke(language: AppLanguage) = manager.saveLanguage(language)
}