package naivebayes

import java.util.HashMap
import kotlin.math.log
import kotlin.math.log10
import kotlin.math.log2

class NaiveBayes {

    private var logLikelihoods: Map<String, Map<String, Double>> = HashMap()
    private var categoryCount = 0
    //var featureCategory: Map<String, >

    fun computePriors(category: String, total: Int, wordsOccurrences: Map<String, Int>) {
        categoryCount++
        var likelihoods = HashMap<String, Double>()
        for (entry in wordsOccurrences.entries) {
            likelihoods.put(entry.key, Math.log((entry.value.toDouble() / total)))
        }
        logLikelihoods = logLikelihoods.plus(Pair(category, likelihoods))
    }

    fun computeProbability(words: HashMap<String, Int>) {
        var maxProbability = 0.0
        var maxCategory = ""
        for (categoryEntry in logLikelihoods)
        {
            var found = 0
            var probability = 0.0
            var likelihoods = categoryEntry.value
            for (entry in words)
            {
                if (likelihoods.containsKey(entry.key))
                    found++
                probability += likelihoods.getOrDefault(entry.key, 0.0) * entry.value
            }
            println(categoryEntry.key + " " + found)
            probability /= categoryCount

            if (probability < maxProbability) {
                maxProbability = probability
                maxCategory = categoryEntry.key
            }
        }
        println(maxCategory + " " + maxProbability)
    }
}