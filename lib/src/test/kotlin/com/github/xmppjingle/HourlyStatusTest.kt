package com.github.xmppjingle

import com.github.xmppjingle.geo.GeoUtils
import com.github.xmppjingle.statistical.HomeWorkProfile
import com.github.xmppjingle.statistical.HomeworkScore.Companion.calculateBonus
import com.github.xmppjingle.statistical.HomeworkScore.Companion.calculateScores
import com.github.xmppjingle.statistical.HomeworkScore.Companion.calculateTotalScoreByPostCode
import com.github.xmppjingle.statistical.HomeworkScore.Companion.compareTimetables
import com.github.xmppjingle.statistical.HomeworkScore.Companion.getHomeWorkProfile
import com.github.xmppjingle.statistical.HourlyStatus
import com.github.xmppjingle.statistical.ScoreParserUtils
import com.github.xmppjingle.statistical.ScoreParserUtils.Companion.createPNGFromTimetable
import com.github.xmppjingle.statistical.ScoreParserUtils.Companion.pngToTimetable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.Month
import javax.imageio.ImageIO

class HourlyStatusTest {

    @Test
    fun `calculateScores should return correct scores`() {
        val start = LocalDateTime.of(2023, Month.JANUARY, 2, 8, 0)
        val end = LocalDateTime.of(2023, Month.JANUARY, 2, 18, 0)
        val expected = mapOf(HourlyStatus.WORK to 10, HourlyStatus.HOME to 0, HourlyStatus.OTHER to 0)
        val actual = calculateScores(start, end, defaultTimetable)
        assertEquals(expected, actual)
    }

    @Test
    fun `calculateBonus should return correct scores`() {
        val scores = mapOf(HourlyStatus.WORK to 10, HourlyStatus.HOME to 0, HourlyStatus.OTHER to 0)
        val expected = mapOf(HourlyStatus.WORK to 13, HourlyStatus.HOME to 0, HourlyStatus.OTHER to 0)
        val actual = calculateBonus(scores)
        assertEquals(expected, actual)
    }

    @Test
    fun `calculateTotalScoreByPostCode should return correct scores`() {
        File("src/test/resources/timetables").walk().filter { it.isFile }.forEach {

            val input = FileInputStream(it)
            val records = ScoreParserUtils.readAndParseFile(input)

//            hashAndWriteToCsv(it, File("${it.name}-hashed.csv"), listOf("endCity.postCode"))

            val timetable = pngToTimetable(ImageIO.read(File("defaultTimetable.png")) as BufferedImage)

            val mergedRecords = ScoreParserUtils.mergeRecordsByPostcode(records)

            val result = getHomeWorkProfile(calculateTotalScoreByPostCode(records, timetable))

            println("${(result)} - ${it.name}")

            val resultGrouped =
                getHomeWorkProfile(calculateTotalScoreByPostCode(mergedRecords, timetable))

            println("${(resultGrouped)} - ${it.name}")

            val expected = parseFilename(it.name)

            assertEquals(expected, (result))
            assertEquals(expected, (resultGrouped))

        }

    }

    @Test
    fun `simple calculateTotalScoreByPostCode should return correct scores`() {
        File("src/test/resources/timetables").walk().filter { it.isFile }.forEach {

            val input = FileInputStream(it)
            val records = ScoreParserUtils.readAndParseFile(input)
            val timetable = pngToTimetable(ImageIO.read(File("defaultTimetable.png")) as BufferedImage)

            val result = getHomeWorkProfile(calculateTotalScoreByPostCode(records, timetable))

            println("${(result)} - ${it.name}")

            val expected = parseFilename(it.name)

            assertEquals(expected, (result))

        }

    }

    private fun parseFilename(filename: String): HomeWorkProfile {
        val parts = filename.split("-")
        val homePostCode = parts[1]
        val workPostCode = parts[2]
        val workFromHome = parts[3].split(".")[0] == "true"
        return HomeWorkProfile(homePostCode, workPostCode, workFromHome)
    }

    @Test
    fun `create PNG for defaultTimetable`() {
        createPNGFromTimetable(defaultTimetable, "defaultTimetable.png")
        val t = pngToTimetable(ImageIO.read(File("defaultTimetable.png")) as BufferedImage)
        println(compareTimetables(defaultTimetable, t))
    }

    val defaultTimetable: Map<DayOfWeek, List<HourlyStatus>> = mapOf(DayOfWeek.MONDAY to List(24) { i ->
        when (i) {
            in 8..18 -> HourlyStatus.WORK
            in 20..23, in 0..6 -> HourlyStatus.HOME
            else -> HourlyStatus.OTHER
        }
    }, DayOfWeek.TUESDAY to List(24) { i ->
        when (i) {
            in 8..18 -> HourlyStatus.WORK
            in 20..23, in 0..6 -> HourlyStatus.HOME
            else -> HourlyStatus.OTHER
        }
    }, DayOfWeek.WEDNESDAY to List(24) { i ->
        when (i) {
            in 8..18 -> HourlyStatus.WORK
            in 20..23, in 0..6 -> HourlyStatus.HOME
            else -> HourlyStatus.OTHER
        }
    }, DayOfWeek.THURSDAY to List(24) { i ->
        when (i) {
            in 8..18 -> HourlyStatus.WORK
            in 20..23, in 0..6 -> HourlyStatus.HOME
            else -> HourlyStatus.OTHER
        }
    }, DayOfWeek.FRIDAY to List(24) { i ->
        when (i) {
            in 8..18 -> HourlyStatus.WORK
            in 20..23, in 0..6 -> HourlyStatus.HOME
            else -> HourlyStatus.OTHER
        }
    }, DayOfWeek.SATURDAY to List(24) { i ->
        when (i) {
            in 9..12 -> HourlyStatus.WORK
            in 13..20 -> HourlyStatus.OTHER
            in 20..23, in 0..6 -> HourlyStatus.HOME
            else -> HourlyStatus.OTHER
        }
    }, DayOfWeek.SUNDAY to List(24) { i ->
        when (i) {
            in 9..12 -> HourlyStatus.WORK
            in 13..20 -> HourlyStatus.OTHER
            in 20..23, in 0..6 -> HourlyStatus.HOME
            else -> HourlyStatus.OTHER
        }
    })

}
