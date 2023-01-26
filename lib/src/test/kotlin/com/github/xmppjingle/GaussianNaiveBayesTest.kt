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

    @Test
    fun `test gaussian naive bayes with extract`() {

        val data = mutableListOf<Pair<List<Double>, String>>()

        BufferedReader(InputStreamReader(javaClass.getResourceAsStream("/training_data.csv"))).use { reader ->
            reader.readLine() // skip the header
            var line = reader.readLine()
            while (line != null) {
                val values = line.split(',')
                val timeOfDay = values[1].toDouble()
                val splitTimeOfDay = GaussianNaiveBayes.splitTimeOfDay(timeOfDay)
                val features =
                    values.subList(0, 1).map { it.toDouble() } + splitTimeOfDay + values.subList(2, values.size - 2)
                        .map { it.toDouble() }
                data.add(Pair(features, values[values.size - 1]))
                line = reader.readLine()
            }
        }

        // Train the classifier
        val classifier = GaussianNaiveBayes.train(data)

        val dataCheck = mutableListOf<Pair<List<Double>, String>>()
        val origData = mutableListOf<List<String>>()

        BufferedReader(InputStreamReader(javaClass.getResourceAsStream("/extract_personal.csv"))).use { reader ->
//            reader.readLine() // skip the header
            var line = reader.readLine()
            while (line != null) {
                val allValues = line.split(',')
                val values = allValues.dropLast(2)
                val timeOfDay = values[1].toDouble()
                val splitTimeOfDay = GaussianNaiveBayes.splitTimeOfDay(timeOfDay)
                val features =
                    values.subList(0, 1).map { it.toDouble() } + splitTimeOfDay + values.subList(2, values.size - 2)
                        .map { it.toDouble() }
//                if (allValues[5] == "783e57d91443e1cfec4e857154565237") {
                    dataCheck.add(Pair(features, values[values.size - 1]))
                    origData.add(allValues)
//                }
                line = reader.readLine()
            }
        }

        listOf(
            listOf("5400", "14", "2"),
            listOf("3600", "11", "2"),
            listOf("360", "8", "2"),
            listOf("3000", "11", "2"),
            listOf("6000", "7", "2"),
            listOf("6000", "17", "2")
        ).map { it.map { it.toDouble() } }.forEach {
            val prediction = classifier.predict(GaussianNaiveBayes.splitInputTimeOfDay(it))
            println("Prediction(${it}): $prediction")
        }

        println("\n\n")

        val dTree = mutableMapOf<String, MutableMap<String, Int>>()
        val uTree = mutableMapOf<String, MutableMap<String, AtomicInteger>>()

        var correctCount = 0
        dataCheck.forEachIndexed { i, it ->
            val prediction = classifier.predict(it.first)
            incrementD(dTree, origData[i][5], origData[i][4], prediction)
            incrementU(uTree, origData[i][5], origData[i][4], prediction)
            if (prediction == it.second) correctCount++
            println("Prediction(${"${origData[i][0].toDouble() / 60}, ${origData[i][1].toDouble()}, ${origData[i][2]}"}): $prediction/${it.second}")
        }
        println("$correctCount out of ${dataCheck.size} - ${(correctCount.toFloat() / dataCheck.size) * 100}% Accuracy")

        println("Location User Group")
        dTree.forEach {
//            println(it)
        }

        println("User Grouped")
        uTree.forEach {
            println("${it.key} -> ${it.value.values}")
        }
    }

    private fun incrementD(
        t: MutableMap<String, MutableMap<String, Int>>,
        user: String,
        rLocation: String,
        prediction: String
    ) {
        t["${user}_${rLocation}"]?.let { group ->
            group[prediction]?.let {
                group[prediction] = it + 1
                prediction
            } ?: group.put(prediction, 1)
            prediction
        }
            ?: t.put("${user}_${rLocation}", mutableMapOf(prediction to 1))

    }

    private fun incrementU(
        t: MutableMap<String, MutableMap<String, AtomicInteger>>,
        user: String,
        rLocation: String,
        prediction: String
    ) {
        val k = "${rLocation}_$prediction"
        t[user]?.let { userGroup ->
            userGroup[k]?.incrementAndGet()
                ?: userGroup.put(k, AtomicInteger(1))
            prediction
        }
            ?: t.put(user, mutableMapOf(k to AtomicInteger(1)))

    }


    @Test
    fun `test gaussian naive bayes large preset`() {
        val data = mutableListOf<Pair<List<Double>, String>>()

        BufferedReader(InputStreamReader(javaClass.getResourceAsStream("/homework_preset.csv"))).use { reader ->
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

    /* Example of input:
    "@timestamp",purpose,startTime,endTime,userId,"beginCity.location"
"Jan 2, 2023 @ 22:13:27.638",eat,"Jan 2, 2023 @ 19:45:47.000","Jan 2, 2023 @ 21:55:14.000","bb3e4b20-25f7-4688-b690-78c80a6059cb","{
  ""coordinates"": [
    4.4938780384615375,
    52.16942593846154
  ],
  ""type"": ""Point""
}"
"Jan 2, 2023 @ 19:56:38.420",home,"Jan 1, 2023 @ 19:22:04.000","Jan 2, 2023 @ 19:36:26.000","bb3e4b20-25f7-4688-b690-78c80a6059cb","{
  ""coordinates"": [
    4.500238588235295,
    52.17435721764705
  ],
  ""type"": ""Point""
}"
     */

    @Test
    fun checkConvertionOfGeneralCSVtoNormalizedTrainingDataCSV() {

        javaClass.getResourceAsStream("/general_test1.csv").let { reader ->
            val parsed = GeneralDataParser.parseInputData(reader)

            val file = File("extract_personal.csv")
            val writer = BufferedWriter(file.writer())
            writer.write(parsed)

            writer.close()

        }

    }



}

