package com.github.xmppjingle.geo

import kotlin.math.cos
import kotlin.math.roundToInt

class GeoUtils {
    companion object {
        fun roundLocation(location: Location, accuracyInKm: Double = .6): Location =
            location.lon?.let { lon ->
                location.lat?.let { lat ->
                    if (lat > 0) {
                        val degrees = Math.toDegrees(accuracyInKm / 6371.0)
                        val roundLat = (lat / degrees).roundToInt() * degrees
                        val cos = cos(Math.toRadians(roundLat))
                        val degreesLonGrid = degrees / cos
                        Location(
                            (lon / degreesLonGrid).roundToInt() * degreesLonGrid,
                            roundLat)
                    } else {
                        Location(lon.round(5), lat)
                    }
                }
            } ?: location

        private fun Double.round(decimals: Int): Double {
            var multiplier = 1.0
            repeat(decimals) { multiplier *= 10 }
            return kotlin.math.round(this * multiplier) / multiplier
        }
    }
}

data class Location(
    val lon: Double?,
    val lat: Double?
)