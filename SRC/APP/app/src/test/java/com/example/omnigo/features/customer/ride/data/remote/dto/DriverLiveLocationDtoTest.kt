package com.example.omnigo.features.customer.ride.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DriverLiveLocationDtoTest {

    @Test
    fun `toDomain with valid properties maps successfully to domain model`() {
        val dto = DriverLiveLocationDto(
            driverId = 88L,
            latitude = 10.7725,
            longitude = 106.6980,
            bearing = 120.0f,
            speed = 8.5f,
            timestamp = 1690000000000L
        )

        val domain = dto.toDomain()

        assertNotNull(domain)
        assertEquals(88L, domain?.driverId)
        assertEquals(10.7725, domain?.latitude ?: 0.0, 0.0001)
        assertEquals(106.6980, domain?.longitude ?: 0.0, 0.0001)
        assertEquals(120.0f, domain?.bearing ?: 0f, 0.1f)
        assertEquals(8.5f, domain?.speed ?: 0f, 0.1f)
        assertEquals(1690000000000L, domain?.timestamp)
    }

    @Test
    fun `toDomain with missing driverId returns null`() {
        val dto = DriverLiveLocationDto(
            driverId = null,
            latitude = 10.7725,
            longitude = 106.6980
        )
        assertNull(dto.toDomain())
    }

    @Test
    fun `toDomain with zero or negative driverId returns null`() {
        val dtoZero = DriverLiveLocationDto(driverId = 0L, latitude = 10.0, longitude = 106.0)
        val dtoNegative = DriverLiveLocationDto(driverId = -10L, latitude = 10.0, longitude = 106.0)
        assertNull(dtoZero.toDomain())
        assertNull(dtoNegative.toDomain())
    }

    @Test
    fun `toDomain with missing latitude or longitude returns null`() {
        val dtoNoLat = DriverLiveLocationDto(driverId = 1L, latitude = null, longitude = 106.0)
        val dtoNoLng = DriverLiveLocationDto(driverId = 1L, latitude = 10.0, longitude = null)
        assertNull(dtoNoLat.toDomain())
        assertNull(dtoNoLng.toDomain())
    }
}
