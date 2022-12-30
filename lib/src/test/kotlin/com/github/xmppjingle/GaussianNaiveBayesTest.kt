package com.github.xmppjingle

import com.github.xmppjingle.bayes.GaussianNaiveBayes
import kotlin.test.Test
import java.io.BufferedReader
import java.io.InputStreamReader
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
                    values.subList(0, 1).map { it.toDouble() } + splitTimeOfDay + values.subList(2, values.size - 1)
                        .map { it.toDouble() }
                data.add(Pair(features, values[values.size - 1]))
                line = reader.readLine()
            }
        }

        // Train the classifier
        val classifier = GaussianNaiveBayes.train(data)

        val dataCheck = mutableListOf<Pair<List<Double>, String>>()
        val origData = mutableListOf<List<String>>()

        BufferedReader(InputStreamReader(javaClass.getResourceAsStream("/extract.csv"))).use { reader ->
            reader.readLine() // skip the header
            var line = reader.readLine()
            while (line != null) {
                val allValues = line.split(',')
                val values = allValues.dropLast(2)
                val timeOfDay = values[1].toDouble()
                val splitTimeOfDay = GaussianNaiveBayes.splitTimeOfDay(timeOfDay)
                val features =
                    values.subList(0, 1).map { it.toDouble() } + splitTimeOfDay + values.subList(2, values.size - 1)
                        .map { it.toDouble() }
//                if(allValues[5] == "3ecd496815a87e652b519612d381d548") {
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
//            println("Prediction(${"${origData[i][0].toDouble() / 60}, ${origData[i][1].toDouble()}, ${origData[i][2]}"}): $prediction/${it.second}")
        }
        println("$correctCount out of ${dataCheck.size} - ${(correctCount.toFloat() / dataCheck.size) * 100}% Accuracy")

        println("Location User Group")
        dTree.forEach{
//            println(it)
        }

        println("User Grouped")
        uTree.forEach{
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
                ?:
                userGroup.put(k, AtomicInteger(1))
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

}

