package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.food.data.remote.realtime.FoodStompSubscriptionSource
import com.example.omnigo.features.driver.data.mapper.DriverFoodOfferMapper
import com.example.omnigo.features.driver.domain.model.DriverFoodOffer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class ObserveDriverFoodOffersUseCase @Inject constructor(
    private val subscriptionSource: FoodStompSubscriptionSource
) {
    operator fun invoke(): Flow<DriverFoodOffer> {
        return subscriptionSource.subscribeToFoodOffers()
            .mapNotNull { dto ->
                DriverFoodOfferMapper.mapToDomain(dto)
            }
    }
}
