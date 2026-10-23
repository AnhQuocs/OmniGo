package com.example.omnigo.features.customer.food.di

import com.example.omnigo.features.customer.food.data.remote.api.FoodApi
import com.example.omnigo.features.customer.food.data.remote.api.FoodReviewApi
import com.example.omnigo.features.customer.food.data.repository.ActiveFoodOrderTrackingRepositoryImpl
import com.example.omnigo.features.customer.food.data.repository.FoodRealtimeRepositoryImpl
import com.example.omnigo.features.customer.food.data.repository.FoodRepositoryImpl
import com.example.omnigo.features.customer.food.data.repository.FoodReviewRepositoryImpl
import com.example.omnigo.features.customer.food.domain.repository.ActiveFoodOrderTrackingRepository
import com.example.omnigo.features.customer.food.domain.repository.FoodRealtimeRepository
import com.example.omnigo.features.customer.food.domain.repository.FoodRepository
import com.example.omnigo.features.customer.food.domain.repository.FoodReviewRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FoodModule {

    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        foodRepositoryImpl: FoodRepositoryImpl
    ): FoodRepository

    @Binds
    @Singleton
    abstract fun bindActiveFoodOrderTrackingRepository(
        impl: ActiveFoodOrderTrackingRepositoryImpl
    ): ActiveFoodOrderTrackingRepository

    @Binds
    @Singleton
    abstract fun bindFoodRealtimeRepository(
        impl: FoodRealtimeRepositoryImpl
    ): FoodRealtimeRepository

    @Binds
    @Singleton
    abstract fun bindFoodReviewRepository(
        impl: FoodReviewRepositoryImpl
    ): FoodReviewRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FoodNetworkModule {

    @Provides
    @Singleton
    fun provideFoodApi(retrofit: Retrofit): FoodApi {
        return retrofit.create(FoodApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFoodReviewApi(retrofit: Retrofit): FoodReviewApi {
        return retrofit.create(FoodReviewApi::class.java)
    }
}
