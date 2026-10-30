package com.example.omnigo.features.driver.earnings.di

import com.example.omnigo.features.driver.earnings.data.remote.api.DriverPaymentApi
import com.example.omnigo.features.driver.earnings.data.repository.DriverPaymentRepositoryImpl
import com.example.omnigo.features.driver.earnings.domain.repository.DriverPaymentRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DriverPaymentRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDriverPaymentRepository(
        impl: DriverPaymentRepositoryImpl
    ): DriverPaymentRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DriverPaymentNetworkModule {

    @Provides
    @Singleton
    fun provideDriverPaymentApi(retrofit: Retrofit): DriverPaymentApi {
        return retrofit.create(DriverPaymentApi::class.java)
    }
}

