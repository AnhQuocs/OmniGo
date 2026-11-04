package com.example.omnigo.features.customer.ride.presentation.viewmodel

import com.example.omnigo.R
import com.example.omnigo.core.location.tracker.OsrmRoutingService
import com.example.omnigo.features.auth.domain.model.VehicleType
import com.example.omnigo.features.customer.ride.domain.model.CalculateRidePricingResult
import com.example.omnigo.features.customer.ride.domain.model.PricingEstimate
import com.example.omnigo.features.customer.ride.domain.usecase.CalculateRidePricingUseCase
import com.example.omnigo.features.customer.ride.presentation.state.LocationPoint
import com.example.omnigo.utils.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import javax.inject.Inject

data class RidePricingCalculationResult(
    val estimates: Map<VehicleType, PricingEstimate>,
    val selectedEstimate: PricingEstimate?,
    val error: UiText?,
    val routeCoordinates: List<Pair<Double, Double>>
)

class RidePricingHelper @Inject constructor(
    private val calculateRidePricingUseCase: CalculateRidePricingUseCase,
    private val osrmRoutingService: OsrmRoutingService
) {
    suspend fun calculate(
        scope: CoroutineScope,
        pickup: LocationPoint,
        dropoff: LocationPoint,
        selectedType: VehicleType,
        nearbyCount: Int
    ): RidePricingCalculationResult {
        val bikeDeferred = scope.async { calculateRidePricingUseCase(pickup.latitude, pickup.longitude, dropoff.latitude, dropoff.longitude, nearbyDriversCount = nearbyCount, vehicleType = "BIKE") }
        val car4Deferred = scope.async { calculateRidePricingUseCase(pickup.latitude, pickup.longitude, dropoff.latitude, dropoff.longitude, nearbyDriversCount = nearbyCount, vehicleType = "CAR_4_SEAT") }
        val car7Deferred = scope.async { calculateRidePricingUseCase(pickup.latitude, pickup.longitude, dropoff.latitude, dropoff.longitude, nearbyDriversCount = nearbyCount, vehicleType = "CAR_7_SEAT") }
        val routeDeferred = scope.async { osrmRoutingService.getRouteCoordinates(pickup.latitude, pickup.longitude, dropoff.latitude, dropoff.longitude, selectedType.name) }

        val bikeRes = bikeDeferred.await()
        val car4Res = car4Deferred.await()
        val car7Res = car7Deferred.await()
        val routeCoords = routeDeferred.await()

        val estimates = buildMap {
            if (bikeRes is CalculateRidePricingResult.Success) put(VehicleType.BIKE, bikeRes.estimate)
            if (car4Res is CalculateRidePricingResult.Success) put(VehicleType.CAR_4_SEAT, car4Res.estimate)
            if (car7Res is CalculateRidePricingResult.Success) put(VehicleType.CAR_7_SEAT, car7Res.estimate)
        }
        val selectedResult = when (selectedType) {
            VehicleType.BIKE -> bikeRes
            VehicleType.CAR_4_SEAT -> car4Res
            VehicleType.CAR_7_SEAT -> car7Res
            else -> null
        }
        val errorUiText: UiText? = when {
            estimates.isEmpty() -> (bikeRes as? CalculateRidePricingResult.Error)?.message?.let { UiText.DynamicString(it) }
                ?: (car4Res as? CalculateRidePricingResult.Error)?.message?.let { UiText.DynamicString(it) }
                ?: (car7Res as? CalculateRidePricingResult.Error)?.message?.let { UiText.DynamicString(it) }
                ?: UiText.StringResource(R.string.ride_pricing_failed)
            selectedResult is CalculateRidePricingResult.Error -> selectedResult.message?.let { UiText.DynamicString(it) }
            else -> null
        }
        return RidePricingCalculationResult(estimates, estimates[selectedType], errorUiText, routeCoords)
    }
}
