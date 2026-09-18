package com.example.omnigo.features.customer.food.di

import com.example.omnigo.features.customer.food.data.remote.api.FoodApi
import com.example.omnigo.features.customer.food.data.repository.FoodRepositoryImpl
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FoodRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        foodRepositoryImpl: FoodRepositoryImpl
    ): FoodRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FoodNetworkModule {

    @Provides
    @Singleton
    fun provideFoodApi(retrofit: Retrofit): FoodApi {
        return retrofit.create(FoodApi::class.java)
    }
}
