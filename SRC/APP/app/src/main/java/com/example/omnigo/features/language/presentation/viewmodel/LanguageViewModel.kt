package com.example.omnigo.features.language.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omnigo.features.language.domain.model.AppLanguage
import com.example.omnigo.features.language.domain.usecase.LanguageUseCases
import com.example.omnigo.utils.LangUtils
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val languageUseCases: LanguageUseCases
) : ViewModel() {

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    init {
        viewModelScope.launch {
            languageUseCases.getLanguageUseCase().collect {
                LangUtils.currentLang = it.code
                _currentLanguage.value = it
            }
        }
    }

    fun changeLanguage(language: AppLanguage) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            languageUseCases.updateLanguageUseCase(language)
        }
    }
}