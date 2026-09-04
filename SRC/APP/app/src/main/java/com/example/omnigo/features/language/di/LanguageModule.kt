package com.example.omnigo.features.language.di

import android.content.Context
import com.example.omnigo.features.language.data.preference.LanguagePreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LanguageModule {

    @Provides
    @Singleton
    fun provideLanguagePreferenceManager(
        @ApplicationContext context: Context
    ): LanguagePreferenceManager = LanguagePreferenceManager(context)
}