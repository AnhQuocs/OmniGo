package com.example.omnigo.features.customer.ride.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.DriverLiveLocation
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class ObserveDriverLiveLocationUseCase @Inject constructor(
    private val rideBookingRepository: RideBookingRepository
) {
    operator fun invoke(driverId: Long): Flow<DriverLiveLocation> {
        if (driverId <= 0L) return emptyFlow()
        return rideBookingRepository.observeDriverLiveLocation(driverId)
    }
}
