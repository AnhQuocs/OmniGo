package com.example.omnigo.utils

/**
 * Decodes a Google-encoded polyline string into a list of (latitude, longitude) pairs.
 * Used for OSRM / Google Maps polyline strings.
 */
object PolylineDecoder {

    fun decode(encoded: String?): List<Pair<Double, Double>> {
        if (encoded.isNullOrBlank()) return emptyList()

        val poly = ArrayList<Pair<Double, Double>>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                if (index >= len) break
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                if (index >= len) break
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latitude = lat.toDouble() / 1E5
            val longitude = lng.toDouble() / 1E5
            poly.add(Pair(latitude, longitude))
        }

        return poly
    }
}
