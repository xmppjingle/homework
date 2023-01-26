package com.github.xmppjingle

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.xmppjingle.geo.GeoUtils
import com.github.xmppjingle.geo.Location
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GeneralDataParser {
    companion object {
        fun parseInputDataAsList(reader: InputStream): List<String> {
            val result = mutableListOf<String>()
            CsvReader().readAllWithHeader(reader).forEach { row ->
                val timestamp = row["@timestamp"]
                val purpose = row["purpose"]
                val startTime = row["startTime"]
                val endTime = row["endTime"]
                val userId = row["userId"]
                val locationJson = row["beginCity.location"]
                val city = row["endCity.locality"]

                // Parse the location json and convert to Location object
                val location = convertLocationJson(locationJson!!)
                val roundLocation = GeoUtils.roundLocation(location, 1.1).let { "${it.lat}_${it.lon}" }
                val zipCode = row["endCity.postCode"]?.replace("\\s".toRegex(), "")

                // Calculate the time of day
                val timeOfDay = SimpleDateFormat("MMM dd, yyyy '@' HH:mm:ss.SSS").parse(startTime).hours

                // Calculate the duration of stay
                val durationOfStay = calculateDuration(startTime!!, endTime!!) * 60

                val dayType = typeOfDay(startTime)

                // Determine the category
                val category = determineCategory(purpose!!)

                // Append the data to the result string
                result.add("$durationOfStay,$timeOfDay,$dayType,$category,$zipCode,$userId,$city")
            }
            return result
        }

        fun parseInputData(reader: InputStream): String {
            return mergeSequentialLines(parseInputDataAsList(reader)).joinToString("\n")
        }

        fun typeOfDay(datetime: String): Int {
            val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy @ HH:mm:ss.SSS")
            val dateTime = LocalDateTime.parse(datetime, formatter)
            val dayOfWeek = dateTime.dayOfWeek
            return if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                -2
            } else {
                2
            }
        }

        fun determineCategory(purpose: String): String {
            return when (purpose.toLowerCase()) {
                "home" -> "Home"
                "work" -> "Work"
                else -> "Other"
            }
        }

        fun convertLocationJson(locationJson: String): Location {
            // Use the Jackson library to parse the json and convert to Location object
            return convertLocation(parseLocation(locationJson))
        }

        fun calculateDuration(startTime: String, endTime: String): Long {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy '@' HH:mm:ss.SSS")
            val startDate = dateFormat.parse(startTime)
            val endDate = dateFormat.parse(endTime)
            val duration = endDate.time - startDate.time  // duration in milliseconds
            return duration / (1000 * 60)  // convert duration to hours
        }

        data class JSONLocation(val coordinates: List<Double>, val type: String)

        fun parseLocation(locationJson: String): JSONLocation {
            val objectMapper = ObjectMapper().registerKotlinModule()
            return objectMapper.readValue(locationJson, JSONLocation::class.java)
        }

        fun convertLocation(location: JSONLocation): Location {
            return Location(location.coordinates[0], location.coordinates[1])
        }

        fun mergeSequentialLines(lines: List<String>): List<String> {
            val mergedLines = mutableListOf<String>()
            var currentLine: Array<String> = lines[0].split(",").toTypedArray()
            for (i in 1 until lines.size) {
                val line = lines[i].split(",").toTypedArray()
                if (line[4] == currentLine[4]) {
                    currentLine[0] = (currentLine[0].toInt() + line[0].toInt()).toString()
                    currentLine[1] = minOf(currentLine[1].toInt(), line[1].toInt()).toString()
                    currentLine[2] = maxOf(currentLine[2].toInt(), line[2].toInt()).toString()
                } else {
                    mergedLines.add(currentLine.joinToString(","))
                    currentLine = line
                }
            }
            mergedLines.add(currentLine.joinToString(","))
            return mergedLines
        }

    }
}