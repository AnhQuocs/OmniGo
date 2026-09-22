package com.example.omnigo.core.di

import android.content.Context
import com.example.omnigo.core.location.tracker.DefaultLocationTracker
import com.example.omnigo.core.location.tracker.LocationTracker
import com.example.omnigo.core.region.data.repository.AdministrativeRegionRepositoryImpl
import com.example.omnigo.core.region.domain.repository.AdministrativeRegionRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindLocationTracker(
        defaultLocationTracker: DefaultLocationTracker
    ): LocationTracker

    @Binds
    @Singleton
    abstract fun bindAdministrativeRegionRepository(
        impl: AdministrativeRegionRepositoryImpl
    ): AdministrativeRegionRepository

    @Binds
    @Singleton
    abstract fun bindSavedAddressRepository(
        impl: com.example.omnigo.features.customer.home.data.repository.SavedAddressRepositoryImpl
    ): com.example.omnigo.features.customer.home.domain.repository.SavedAddressRepository

    companion object {
        @Provides
        @Singleton
        fun provideFusedLocationProviderClient(
            @ApplicationContext context: Context
        ): FusedLocationProviderClient {
            return LocationServices.getFusedLocationProviderClient(context)
        }
    }
}
