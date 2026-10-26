package com.example.omnigo.features.customer.ride.data.mapper

import com.example.omnigo.features.customer.ride.data.remote.dto.DriverLocationResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbyDriverMapperTest {

    @Test
    fun `maps valid DriverLocationResponse to domain NearbyDriver`() {
        val dto = DriverLocationResponse(
            driverId = 101L,
            latitude = 10.776889,
            longitude = 106.700806,
            distanceInKm = 1.2
        )

        val domain = dto.toDomain()

        assertNotNull(domain)
        assertEquals(101L, domain?.driverId)
        assertEquals(10.776889, domain?.latitude ?: 0.0, 0.00001)
        assertEquals(106.700806, domain?.longitude ?: 0.0, 0.00001)
        assertEquals(1.2, domain?.distanceInKm ?: 0.0, 0.0001)
    }

    @Test
    fun `returns null if driverId or coordinates are missing`() {
        val dtoMissingId = DriverLocationResponse(driverId = null, latitude = 10.0, longitude = 106.0)
        val dtoMissingLat = DriverLocationResponse(driverId = 1L, latitude = null, longitude = 106.0)
        val dtoMissingLng = DriverLocationResponse(driverId = 1L, latitude = 10.0, longitude = null)

        assertNull(dtoMissingId.toDomain())
        assertNull(dtoMissingLat.toDomain())
        assertNull(dtoMissingLng.toDomain())
    }

    @Test
    fun `maps list of DriverLocationResponse filtering null items`() {
        val list = listOf(
            DriverLocationResponse(driverId = 1L, latitude = 10.0, longitude = 106.0, distanceInKm = 0.5),
            DriverLocationResponse(driverId = null, latitude = 10.1, longitude = 106.1),
            DriverLocationResponse(driverId = 2L, latitude = 10.2, longitude = 106.2, distanceInKm = 1.8)
        )

        val domainList = list.toDomain()

        assertEquals(2, domainList.size)
        assertEquals(1L, domainList[0].driverId)
        assertEquals(2L, domainList[1].driverId)
    }

    @Test
    fun `null list returns empty list`() {
        val nullList: List<DriverLocationResponse>? = null
        assertTrue(nullList.toDomain().isEmpty())
    }
}
