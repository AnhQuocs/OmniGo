package com.example.omnigo.core.di

import com.example.omnigo.core.websocket.StompClientManager
import com.example.omnigo.features.customer.food.data.remote.realtime.FoodStompSubscriptionSource
import com.example.omnigo.features.customer.food.data.remote.realtime.FoodStompSubscriptionSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebSocketOkHttp

@Module
@InstallIn(SingletonComponent::class)
abstract class WebSocketModule {

    @Binds
    @Singleton
    abstract fun bindFoodStompSubscriptionSource(
        impl: FoodStompSubscriptionSourceImpl
    ): FoodStompSubscriptionSource

    companion object {
        @Provides
        @Singleton
        @WebSocketOkHttp
        fun provideWebSocketOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS) // Persistent WebSocket
                .writeTimeout(15, TimeUnit.SECONDS)
                .pingInterval(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
        }
    }
}
