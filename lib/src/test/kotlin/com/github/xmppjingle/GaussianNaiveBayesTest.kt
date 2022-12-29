package com.github.xmppjingle

import com.github.xmppjingle.bayes.GaussianNaiveBayes
import kotlin.test.Test
import java.io.BufferedReader
import java.io.InputStreamReader

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
            if(prediction == it.second) correctCount++
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
                    values.subList(0, 1).map { it.toDouble() } + splitTimeOfDay + values.subList(2, values.size - 1)
                        .map { it.toDouble() }
                data.add(Pair(features, values[values.size - 1]))
                line = reader.readLine()
            }
        }

        // Train the classifier
        val classifier = GaussianNaiveBayes.train(data)

        val dataCheck = mutableListOf<Pair<List<Double>, String>>()
        val origData = mutableListOf<String>()

        BufferedReader(InputStreamReader(javaClass.getResourceAsStream("/extract.csv"))).use { reader ->
            reader.readLine() // skip the header
            var line = reader.readLine()
            while (line != null) {
                val values = line.split(',').dropLast(2)
                val timeOfDay = values[1].toDouble()
                val splitTimeOfDay = GaussianNaiveBayes.splitTimeOfDay(timeOfDay)
                val features =
                    values.subList(0, 1).map { it.toDouble() } + splitTimeOfDay + values.subList(2, values.size - 1)
                        .map { it.toDouble() }
                dataCheck.add(Pair(features, values[values.size - 1]))
                origData.add("${values[0].toDouble()/60}, ${values[1].toDouble()}, ${values[2]}")
                line = reader.readLine()
            }
        }

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
        dataCheck.forEachIndexed { i, it ->
            val prediction = classifier.predict(it.first)
            if(prediction == it.second) correctCount++
            println("Prediction(${origData[i]}): $prediction/${it.second}")
        }
        println("$correctCount out of ${dataCheck.size} - ${(correctCount.toFloat() / dataCheck.size) * 100}% Accuracy")

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
            if(prediction == it.second) correctCount++
            println("Prediction(${it.first}): $prediction/${it.second}")
        }
        println("$correctCount out of ${data.size} - ${(correctCount.toFloat() / data.size) * 100}% Accuracy")

    }

}

