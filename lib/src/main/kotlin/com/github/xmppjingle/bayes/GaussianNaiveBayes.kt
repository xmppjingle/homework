package com.github.xmppjingle.bayes

import org.apache.commons.math3.stat.descriptive.moment.Variance
import kotlin.math.ln
import kotlin.math.pow


class GaussianDistribution(val mean: Double, val std: Double) {
    fun probability(x: Double): Double {
        val exponent = -(x - mean).pow(2.0) / (2.0 * std.pow(2.0))
        return (1.0 / (std * Math.sqrt(2.0 * Math.PI))) * Math.exp(exponent)
    }
}

class GaussianNaiveBayes(private val distributions: Map<String, List<GaussianDistribution>>) {
    companion object {
        private val LABELS = listOf("Work", "Home", "Other")

        fun train(data: List<Pair<List<Double>, String>>): GaussianNaiveBayes {
            val variance = Variance()
            val featureVariances = List(data[0].first.size) { i ->
                variance.evaluate(data.map { it.first[i] }.toDoubleArray())
            }
            val updatedDistributions = mutableMapOf<String, List<GaussianDistribution>>()
            for (label in LABELS) {
                val labelData = data.filter { it.second == label }
                val labelDistributions = List(labelData[0].first.size) { i ->
                    GaussianDistribution(
                        labelData.map { it.first[i] }.average(),
                        Math.sqrt(featureVariances[i])
                    )
                }
                updatedDistributions[label] = labelDistributions
            }
            return GaussianNaiveBayes(updatedDistributions)
        }

        fun splitTimeOfDay(timeOfDay: Double): List<Double> {
            val sin = Math.sin(timeOfDay * (Math.PI / 12)) * 120
            val cos = Math.cos(timeOfDay * (Math.PI / 12)) * 120
            return listOf(sin, cos)
        }

        fun splitInputTimeOfDay(input: List<Double>, index: Int = 1): List<Double> {
            val timeOfDay = input[index]
            val splitTimeOfDay = splitTimeOfDay(timeOfDay)
            return input.subList(0, index) + splitTimeOfDay + input.subList(index + 1, input.size)
        }


    }

    fun predict(features: List<Double>): String {
        var maxLogProb = Double.NEGATIVE_INFINITY
        var maxLabel = ""
        var logProb = 0.0
        for ((label, labelDistributions) in distributions) {
            logProb = ln(1.0 / distributions.size.toDouble())
            for (i in labelDistributions.indices) {
                logProb += ln(labelDistributions[i].probability(features[i]))
            }
            if (logProb >= maxLogProb) {
                maxLogProb = logProb
                maxLabel = label
            }
        }

        if(maxLabel.isBlank()){
            println("Blank: $features")
        }

        return maxLabel
    }
}