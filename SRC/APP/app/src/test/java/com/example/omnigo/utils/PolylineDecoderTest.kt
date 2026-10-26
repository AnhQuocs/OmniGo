package com.example.omnigo.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolylineDecoderTest {

    @Test
    fun `decode returns empty list when input is null or blank`() {
        assertTrue(PolylineDecoder.decode(null).isEmpty())
        assertTrue(PolylineDecoder.decode("").isEmpty())
        assertTrue(PolylineDecoder.decode("   ").isEmpty())
    }

    @Test
    fun `decode correctly decodes sample google encoded polyline`() {
        // "_p~iF~ps|U_ulLnnqC_mqNvxq`@" is sample polyline connecting (38.5, -120.2), (40.7, -120.95), (43.252, -126.453)
        val polyline = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
        val points = PolylineDecoder.decode(polyline)

        assertEquals(3, points.size)
        assertEquals(38.5, points[0].first, 0.001)
        assertEquals(-120.2, points[0].second, 0.001)
        assertEquals(40.7, points[1].first, 0.001)
        assertEquals(-120.95, points[1].second, 0.001)
        assertEquals(43.252, points[2].first, 0.001)
        assertEquals(-126.453, points[2].second, 0.001)
    }
}
