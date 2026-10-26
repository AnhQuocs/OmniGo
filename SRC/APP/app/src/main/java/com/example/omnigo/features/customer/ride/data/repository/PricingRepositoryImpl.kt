package com.example.omnigo.features.customer.ride.data.repository

import com.example.omnigo.features.customer.ride.data.mapper.toDomain
import com.example.omnigo.features.customer.ride.data.mapper.toPricingRequest
import com.example.omnigo.features.customer.ride.data.remote.api.PricingApi
import com.example.omnigo.features.customer.ride.domain.error.RideError
import com.example.omnigo.features.customer.ride.domain.model.CalculateRidePricingResult
import com.example.omnigo.features.customer.ride.domain.repository.PricingRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PricingRepositoryImpl @Inject constructor(
    private val pricingApi: PricingApi
) : PricingRepository {

    override suspend fun calculatePricing(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double,
        nearbyDriversCount: Int,
        vehicleType: String?
    ): CalculateRidePricingResult {
        return try {
            val request = toPricingRequest(
                startLatitude = startLatitude,
                startLongitude = startLongitude,
                endLatitude = endLatitude,
                endLongitude = endLongitude,
                vehicleType = vehicleType
            )
            val response = pricingApi.calculatePricing(
                request = request,
                nearbyDriversCount = nearbyDriversCount
            )
            CalculateRidePricingResult.Success(response.toDomain())
        } catch (e: IOException) {
            CalculateRidePricingResult.Error(
                error = RideError.NETWORK_ERROR,
                message = e.message
            )
        } catch (e: HttpException) {
            CalculateRidePricingResult.Error(
                error = RideError.SERVER_ERROR,
                message = e.message()
            )
        } catch (e: Exception) {
            CalculateRidePricingResult.Error(
                error = RideError.UNKNOWN_ERROR,
                message = e.message
            )
        }
    }
}
