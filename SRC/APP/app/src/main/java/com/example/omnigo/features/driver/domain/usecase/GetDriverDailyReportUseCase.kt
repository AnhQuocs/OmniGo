package com.example.omnigo.features.driver.domain.usecase

import com.example.omnigo.features.customer.ride.domain.model.GetDriverDailyReportResult
import com.example.omnigo.features.customer.ride.domain.repository.RideBookingRepository
import javax.inject.Inject

class GetDriverDailyReportUseCase @Inject constructor(
    private val rideBookingRepository: RideBookingRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): GetDriverDailyReportResult {
        return rideBookingRepository.getDriverDailyReport(forceRefresh)
    }
}
