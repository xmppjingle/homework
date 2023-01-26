package com.github.xmppjingle

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.xmppjingle.bayes.GaussianNaiveBayes
import com.github.xmppjingle.geo.GeoUtils
import com.github.xmppjingle.geo.Location
import java.io.*
import kotlin.test.Test
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

class GaussianNaiveBayesTest {

    @Test
    fun `test gaussian naive bayes`() {
        val data = mutableListOf<Pair<List<Double>, String>>()

        BufferedReader(InputStreamReader(javaClass.getResourceAsStream("/training_data.csv"))).use { reader ->
            reader.readLine() // skip the header
            var line = reader.readLine()
            while (line != null) {
                val values = line.split(',')
                val timeOfDay = values[1].toDouble()
                val splitTimeOfDay = GaussianNaiveBayes.splitTimeOfDay(timeOfDay)
                val features =
                    values.subList(0, 1).map { it.toDouble() } + splitTimeOfDay + values.subList(2, values.size - 1)
                        .map { it.toDouble() }
                data.add(Pair(features, values[values.size - 1]))
                line = reader.readLine()
            }
        }

        // Train the classifier
        val classifier = GaussianNaiveBayes.train(data)

        listOf(
            listOf("540", "14", "2"),
            listOf("360", "11", "2"),
            listOf("60", "8", "2"),
            listOf("300", "11", "2"),
            listOf("600", "7", "2"),
            listOf("600", "17", "2")
        ).map { it.map { it.toDouble() } }.forEach {
            val prediction = classifier.predict(GaussianNaiveBayes.splitInputTimeOfDay(it))
            println("Prediction(${it}): $prediction")
        }

        println("\n\n")

        var correctCount = 0
        data.forEach {
            val prediction = classifier.predict(it.first)
            if (prediction == it.second) correctCount++
            println("Prediction(${it.first}): $prediction/${it.second}")
        }
        println("$correctCount out of ${data.size} - ${(correctCount.toFloat() / data.size) * 100}% Accuracy")

    }
}

