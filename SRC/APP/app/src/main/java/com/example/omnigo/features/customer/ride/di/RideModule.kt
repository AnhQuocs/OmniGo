package com.example.omnigo.features.customer.ride.di

import com.example.omnigo.features.customer.ride.data.remote.api.PricingApi
import com.example.omnigo.features.customer.ride.data.remote.api.RideBookingApi
import com.example.omnigo.features.customer.ride.data.repository.PricingRepositoryImpl
import com.example.omnigo.features.customer.ride.data.repository.RideBookingRepositoryImpl
import com.example.omnigo.features.customer.ride.domain.repository.PricingRepository
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RideModule {

    @Binds
    @Singleton
    abstract fun bindPricingRepository(
        impl: PricingRepositoryImpl
    ): PricingRepository

    @Binds
    @Singleton
    abstract fun bindRideBookingRepository(
        impl: RideBookingRepositoryImpl
    ): RideBookingRepository
}

@Module
@InstallIn(SingletonComponent::class)
object RideNetworkModule {

    @Provides
    @Singleton
    fun providePricingApi(retrofit: Retrofit): PricingApi {
        return retrofit.create(PricingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRideBookingApi(retrofit: Retrofit): RideBookingApi {
        return retrofit.create(RideBookingApi::class.java)
    }
}
