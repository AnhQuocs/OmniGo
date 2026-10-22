package com.example.omnigo.features.customer.food.data.repository

import com.example.omnigo.features.customer.food.data.mapper.toDomain
import com.example.omnigo.features.customer.food.data.remote.realtime.FoodStompSubscriptionSource
import com.example.omnigo.features.customer.food.domain.model.DriverProfile
import com.example.omnigo.features.customer.food.domain.model.FoodOrderTrackingState
import com.example.omnigo.features.customer.food.domain.repository.ActiveFoodOrderTrackingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveFoodOrderTrackingRepositoryImpl @Inject constructor(
    private val stompSource: FoodStompSubscriptionSource,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : ActiveFoodOrderTrackingRepository {

    private val _activeTrackingState = MutableStateFlow<FoodOrderTrackingState?>(null)
    override val activeTrackingState: StateFlow<FoodOrderTrackingState?> = _activeTrackingState.asStateFlow()

    private var trackingJob: Job? = null
    private var locationJob: Job? = null
    private var lastStatusTimestamp: Long? = null

    override suspend fun activate(orderId: Long) {
        clear()
        _activeTrackingState.value = FoodOrderTrackingState(
            orderId = orderId,
            status = null,
            driverProfile = null,
            driverLocation = null
        )

        trackingJob = scope.launch {
            launch {
                stompSource.subscribeToFoodOrderStatus(orderId).collect { dto ->
                    dto.toDomain()?.let { event ->
                        // Validate matching orderId and drop out-of-order events
                        if (event.orderId == orderId) {
                            val eventTime = parseTimestamp(event.timestamp)
                            _activeTrackingState.update { current ->
                                if (current != null) {
                                    val isOutdated = eventTime != null &&
                                        lastStatusTimestamp != null &&
                                        eventTime < lastStatusTimestamp!!
                                    if (isOutdated) {
                                        current
                                    } else {
                                        if (eventTime != null) lastStatusTimestamp = eventTime
                                        current.copy(status = event.status ?: current.status)
                                    }
                                } else {
                                    null
                                }
                            }
                        }
                    }
                }
            }

            launch {
                stompSource.subscribeToFoodOrderAssignments().collect { dto ->
                    dto.toDomain()?.let { event ->
                        if (event.orderId == orderId) {
                            val profile = DriverProfile(
                                driverId = event.driverId,
                                driverName = event.driverName,
                                driverPhone = event.driverPhone,
                                vehiclePlate = event.vehiclePlate,
                                avatarUrl = null
                            )
                            _activeTrackingState.update { current ->
                                current?.copy(driverProfile = profile)
                            }
                            startDriverLocationTracking(event.driverId)
                        }
                    }
                }
            }
        }
    }

    private fun startDriverLocationTracking(driverId: Long) {
        locationJob?.cancel()
        locationJob = scope.launch {
            stompSource.subscribeToDriverLocation(driverId).collect { dto ->
                dto.toDomain()?.let { location ->
                    // Validate matching assigned driverId
                    if (location.driverId == driverId) {
                        _activeTrackingState.update { current ->
                            current?.copy(driverLocation = location)
                        }
                    }
                }
            }
        }
    }

    override suspend fun clear() {
        trackingJob?.cancel()
        trackingJob = null
        locationJob?.cancel()
        locationJob = null
        lastStatusTimestamp = null
        _activeTrackingState.value = null
    }

    private fun parseTimestamp(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        raw.toLongOrNull()?.let { return it }
        return try {
            Instant.parse(raw).toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }
}
