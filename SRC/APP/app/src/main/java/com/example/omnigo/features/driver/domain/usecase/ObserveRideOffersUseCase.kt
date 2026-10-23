package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import com.example.omnigo.features.driver.domain.model.RideOfferEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRideOffersUseCase @Inject constructor(
    private val rideBookingRepository: RideBookingRepository
) {
    operator fun invoke(): Flow<RideOfferEvent> {
        return rideBookingRepository.observeRideOffers()
    }
}
