package com.example.omnigo.features.driver.di

import com.example.omnigo.features.driver.data.remote.api.DriverApi
import com.example.omnigo.features.driver.data.repository.DriverRepositoryImpl
import com.example.omnigo.features.driver.domain.repository.DriverRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DriverRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDriverRepository(
        driverRepositoryImpl: DriverRepositoryImpl
    ): DriverRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DriverNetworkModule {

    @Provides
    @Singleton
    fun provideDriverApi(retrofit: Retrofit): DriverApi {
        return retrofit.create(DriverApi::class.java)
    }
}
