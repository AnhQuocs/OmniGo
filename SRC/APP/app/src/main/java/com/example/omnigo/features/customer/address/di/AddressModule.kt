package com.example.omnigo.features.customer.address.di

import com.example.omnigo.features.customer.address.data.remote.api.UserAddressApi
import com.example.omnigo.features.customer.address.data.repository.SavedAddressRepositoryImpl
import com.example.omnigo.features.customer.address.domain.repository.SavedAddressRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AddressModule {

    @Binds
    @Singleton
    abstract fun bindSavedAddressRepository(
        impl: SavedAddressRepositoryImpl
    ): SavedAddressRepository

    companion object {
        @Provides
        @Singleton
        fun provideUserAddressApi(retrofit: Retrofit): UserAddressApi {
            return retrofit.create(UserAddressApi::class.java)
        }
    }
}

