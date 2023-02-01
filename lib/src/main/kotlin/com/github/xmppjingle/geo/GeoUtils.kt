package com.github.xmppjingle.geo

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File
import java.io.InputStream
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
                            roundLat
                        )
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

        fun splitTimeOfDay(timeOfDay: Double): List<Double> {
            val sin = Math.sin(timeOfDay * (Math.PI / 12)) * 120
            val cos = Math.cos(timeOfDay * (Math.PI / 12)) * 120
            return listOf(sin, cos)
        }

    }
}

data class CityNames(
    val map:HashMap<String, HashMap<String, String>>
){
    fun getCityName(countryCode: String, cityName: String): String =
        map[countryCode.trim().uppercase()]?.get(cityName.trim().lowercase()) ?: cityName
    companion object{
        fun createNormalizedCityNames(file: File): CityNames =
            createNormalizedCityNames(file.inputStream())

        fun createNormalizedCityNames(inputStream: InputStream): CityNames {
            val cityNameMap = HashMap<String, HashMap<String, String>>()

            val reader = csvReader { delimiter = ';' }
            reader.readAllWithHeader(inputStream).forEach { row ->
                val alternateNames = row["Alternate Names"]?.lowercase()?.trim()
                val asciiName = row["ASCII Name"]?.lowercase()?.trim()
                val countryCode = row["Country Code"]?.uppercase()?.trim() ?: "__"
                // Add all alternate names as keys and asciiName as value in the map
                alternateNames?.split(",")?.forEach {
                    val normal = it.trim().lowercase()
                    val ascii = asciiName ?: "UNKNOWN"
                    if (cityNameMap[countryCode] == null) {
                        cityNameMap[countryCode] = hashMapOf(normal to ascii)
                    } else {
                        cityNameMap[countryCode]?.let { cc -> cc[normal] = ascii }
                    }
                }
            }
            return CityNames(cityNameMap)
        }

    }
}

data class Location(
    val lon: Double?,
    val lat: Double?
) {

}