package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.RideStatusEvent
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class ObserveRideBookingStatusUseCase @Inject constructor(
    private val rideBookingRepository: RideBookingRepository
) {
    operator fun invoke(bookingId: Long): Flow<RideStatusEvent> {
        if (bookingId <= 0L) return emptyFlow()
        return rideBookingRepository.observeBookingStatus(bookingId)
    }
}
