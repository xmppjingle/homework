package com.github.xmppjingle.statistical

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

class HomeworkScore {
    companion object {

        val defaultTimetable =
            Thread.currentThread().contextClassLoader.getResource("defaultTimetable.png")?.openStream()?.let {
                ScoreParserUtils.pngToTimetable(ImageIO.read(it) as BufferedImage)
            }

        fun calculateBonus(scores: Map<HourlyStatus, Int>): Map<HourlyStatus, Int> {
            val maxScore = scores.values.maxOrNull()

            return scores.mapValues { (_, value) ->
                if (value == maxScore) {
                    val bonus = 1 + maxScore / 5
                    value + bonus
                } else {
                    value
                }
            }
        }

        fun calculateScores(
            start: LocalDateTime, end: LocalDateTime, timetable: Timetable = defaultTimetable!!
        ): Map<HourlyStatus, Int> {
            val scores = mutableMapOf(
                HourlyStatus.WORK to 0, HourlyStatus.HOME to 0, HourlyStatus.OTHER to 0
            )

            var current = start
            while (current < end) {
                val dayOfWeek = current.dayOfWeek
                val hour = current.hour
                val status = timetable.week[dayOfWeek]?.get(hour) ?: HourlyStatus.OTHER
                scores[status] = scores.getValue(status) + 1
                current = current.plusHours(1)
            }
            return scores
        }

        fun calculateTotalScoreByPostCode(
            records: List<Record>, timetable: Timetable
        ): Map<String, Map<HourlyStatus, Int>> {
            val postCodeScores = mutableMapOf<String, MutableMap<HourlyStatus, Int>>()

            for (record in records) {
                val scores = calculateScores(record.startTime, record.endTime, timetable)
                val bonus = calculateBonus(scores)

                if (postCodeScores.containsKey(record.postCode)) {
                    for (status in HourlyStatus.values()) {
                        postCodeScores[record.postCode]!![status] =
                            postCodeScores[record.postCode]!![status]!! + scores[status]!! + bonus[status]!!
                    }
                } else {
                    postCodeScores[record.postCode] = scores.toMutableMap()
                    for (status in HourlyStatus.values()) {
                        postCodeScores[record.postCode]!![status] =
                            postCodeScores[record.postCode]!![status]!! + bonus[status]!!
                    }
                }
            }

            return postCodeScores
        }

        fun topByHourlyStatus(
            scores: Map<String, Map<HourlyStatus, Int>>, status: HourlyStatus
        ): List<Pair<String, Int>> {
            val topScores = scores.map { (postCode, statusScores) ->
                postCode to statusScores.getOrDefault(status, 0)
            }.toList().sortedByDescending { it.second }
            return topScores
        }

        fun getHomeWorkProfile(scores: Map<String, Map<HourlyStatus, Int>>): HomeWorkProfile {
            val homePostCode = topByHourlyStatus(scores, HourlyStatus.HOME).first().first
            val workPostCodes = topByHourlyStatus(scores, HourlyStatus.WORK)

            val workPostCode = workPostCodes.filter { it.first != homePostCode }.first().first

            val workFromHome = homePostCode == workPostCodes.first().first
            return HomeWorkProfile(homePostCode, workPostCode, workFromHome)
        }

        fun compareTimetables(
            timetable1: Timetable, timetable2: Timetable
        ): Double {
            var matchingHours = 0
            var totalHours = 0

            for (day in DayOfWeek.values()) {
                val hours1 = timetable1.week[day] ?: emptyList()
                val hours2 = timetable2.week[day] ?: emptyList()

                for (i in 0 until maxOf(hours1.size, hours2.size)) {
                    if (i < hours1.size && i < hours2.size && hours1[i] == hours2[i]) {
                        matchingHours++
                    }
                    totalHours++
                }
            }

            return matchingHours.toDouble() / totalHours.toDouble()
        }

    }
}

enum class HourlyStatus {
    WORK, HOME, OTHER
}

data class Timetable(val week: Map<DayOfWeek, List<HourlyStatus>>)

data class HomeWorkProfile(val homePostCode: String, val workPostCode: String, val workFromHome: Boolean)

class ScoreParserUtils {

    companion object {
        fun readAndParseFile(reader: InputStream): List<Record> {
            val csvReader = CsvReader()
            val parsedRecords = mutableListOf<Record>()
            val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy @ HH:mm:ss.SSS")

            csvReader.readAllWithHeader(reader).forEach { row ->
                val timestamp = LocalDateTime.parse(row["@timestamp"], dateFormatter)
                val userId = row["userId"]!!
                val startTime = LocalDateTime.parse(row["startTime"], dateFormatter)
                val endTime = LocalDateTime.parse(row["endTime"], dateFormatter)
                val postCode = row["endCity.postCode"]!!.replace("\\s".toRegex(), "")
                parsedRecords.add(Record(timestamp, userId, startTime, endTime, postCode))
            }

            return parsedRecords
        }

        fun mergeRecordsByPostcode(records: List<Record>): List<Record> {
            val mergedRecords = mutableListOf<Record>()
            var currentRecord = records[0]
            for (i in 1 until records.size) {
                val record = records[i]
                if (record.postCode == currentRecord.postCode) {
                    currentRecord = Record(
                        currentRecord.timestamp,
                        currentRecord.userId,
                        currentRecord.startTime,
                        record.endTime,
                        currentRecord.postCode
                    )
                } else {
                    mergedRecords.add(currentRecord)
                    currentRecord = record
                }
            }
            mergedRecords.add(currentRecord)
            return mergedRecords
        }

        fun createPNGFromTimetable(
            timetable: Timetable, filename: String, width: Int = 24, height: Int = 7
        ) {
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            val graphics = image.createGraphics()

            for (y in 0 until height) {
                val day = DayOfWeek.values()[y % 7]
                val hourStatuses = timetable.week[day]!!

                for (x in 0 until width) {
                    val hour = x % 24
                    val hourStatus = hourStatuses[hour]

                    when (hourStatus) {
                        HourlyStatus.HOME -> graphics.color = Color.RED
                        HourlyStatus.WORK -> graphics.color = Color.GREEN
                        HourlyStatus.OTHER -> graphics.color = Color.YELLOW
                    }

                    graphics.drawRect(x, y, 1, 1)
                }
            }

            ImageIO.write(image, "png", File(filename))
        }

        fun pngToTimetable(image: BufferedImage): Timetable {
            val timetable = mutableMapOf<DayOfWeek, List<HourlyStatus>>()
            val days = DayOfWeek.values()
            //val width = image.width
            //val height = image.height

            for (dayIndex in 0 until 7) {
                val day = days[dayIndex]
                val hourlyStatusList = mutableListOf<HourlyStatus>()

                for (hourIndex in 0 until 24) {
                    val color = image.getRGB(hourIndex, dayIndex)
                    val red = color shr 16 and 0xFF
                    val green = color shr 8 and 0xFF
                    val blue = color and 0xFF

                    val hourlyStatus = when {
                        red > green && red > blue -> HourlyStatus.HOME
                        green > red && green > blue -> HourlyStatus.WORK
                        else -> HourlyStatus.OTHER
                    }
                    hourlyStatusList.add(hourlyStatus)
                }
                timetable[day] = hourlyStatusList
            }
            return Timetable(timetable)
        }

        fun hashAndWriteToCsv(
            inputFile: File, outputFile: File, columnsToHash: List<String>, secretKey: String = "secret"
        ) {
            val reader = CsvReader()
            val records = reader.readAllWithHeader(inputFile)
            val hashedRecords = mutableListOf<MutableMap<String, String>>()

            for (record in records) {
                val hashedRecord = record.toMutableMap()

                for (column in columnsToHash) {
                    if (hashedRecord.containsKey(column)) {
                        val value = hashedRecord[column]
                        val hashedValue = hashValue(value!!, secretKey)
                        hashedRecord[column] = hashedValue
                    }
                }

                hashedRecords.add(hashedRecord)
            }

            copyFirstLine(inputFile, outputFile)

            val csvWriter = CsvWriter()
            csvWriter.writeAll(hashedRecords.map { it.values.toList() }, outputFile, append = true)
        }

        fun copyFirstLine(inputFile: File, outputFile: File) {
            inputFile.bufferedReader().use { inputReader ->
                outputFile.writeText("${inputReader.readLine()}\n")
            }
        }

        fun hashValue(data: String, secret: String): String {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(data.toByteArray())
            return digest.fold("") { str, it -> str + "%02x".format(it) }.substring(0..8)
        }

    }

}

data class Record(
    val timestamp: LocalDateTime,
    val userId: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val postCode: String
)

