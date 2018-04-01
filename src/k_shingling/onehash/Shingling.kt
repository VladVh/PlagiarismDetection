package k_shingling.onehash

import stopwords.NGram
import stopwords.Word
import java.lang.Math.min
import java.security.MessageDigest
import java.util.*
import kotlin.collections.LinkedHashMap

object Shingling {
    val SHINGLE_LENGTH = 5
    val MIN_COUNT = 200
    val WINDOW_SIZE = 30

    fun createShingles(words: ArrayList<Word>): Map<NGram, String> {
        var shinglesMap = LinkedHashMap<NGram, String>()
        val algorithm = MessageDigest.getInstance("MD5")
        for (i in 0 until words.size - SHINGLE_LENGTH) {
            var ngram = NGram(words.subList(i, i + SHINGLE_LENGTH).joinToString { item -> item.toString() },
                    words[i].start,
                    words[i + SHINGLE_LENGTH].end)
            var hash = obtainHashValue(ngram, algorithm)
            shinglesMap.put(ngram, hash)
        }
        return shinglesMap
    }

    private fun obtainHashValue(nGram: NGram, algorithm: MessageDigest): String  {
        var bytes = algorithm.digest(nGram.toString().toByteArray())
        val sb = StringBuffer("")
        for (i in 0 until bytes.size) {
            sb.append(Integer.toString((bytes[i].toInt() and(0xff)) + 0x100, 16).substring(1))
        }
        return sb.toString()
    }

    fun getMinShingles(shingles: Map<NGram, String>): LinkedHashMap<NGram, String> {
        var items = shingles.toList().sortedBy { item -> item.second }.take(MIN_COUNT)
        var map = LinkedHashMap<NGram, String>()
        map.putAll(items)
        return map
    }
    fun compareAgainstDataset(input: Map<NGram, String>) {
        for (item in DataSet.collection) {
            if (compareDocuments(input, item.value)) {
                println(item.key)
                println()
            }
        }
    }

    fun compareDocuments(input: Map<NGram, String>, data: Map<NGram, String>): Boolean {
        var suspicious = input
        var inputMin = getMinShingles(suspicious)
        var dataMin = getMinShingles(data)
        if (compareShingles(inputMin, dataMin) > 0.04) {
            //println("Suspicious")
            var paragraphStart = -1
            var paragraphEnd = -1
            var counter = 0
            while (counter < input.size) {
                var subMap = suspicious.toList().take(WINDOW_SIZE).toMap()
                val result = compareShingles(subMap, data)
                if (result > 0.5) {
                    val start = subMap.minBy { entry -> entry.key.start }?.key?.start!!
                    val end = subMap.maxBy { entry -> entry.key.end }?.key?.end!!
                    if (paragraphStart != -1 && (start - paragraphEnd) < 200) {
                        paragraphEnd = end
                    } else {
                        if (paragraphStart != -1)
                            println("start: $paragraphStart end: $paragraphEnd")
                        paragraphStart = start
                        paragraphEnd = end

                    }
                }
//                    println("start: ${subMap.minBy { entry -> entry.key.start }?.key?.start}  " +
//                            "end: ${subMap.maxBy { entry -> entry.key.end }?.key?.end}")
                suspicious = suspicious.toList().subList(min(WINDOW_SIZE, suspicious.size), suspicious.size).toMap()
                counter += WINDOW_SIZE
            }
            if (paragraphStart != -1)
                println("start: $paragraphStart end: $paragraphEnd")
            return true
        } else {
            return false
        }
    }

    fun compareShingles(input: Map<NGram, String>, data: Map<NGram, String>): Double {
        var count = 0
        for (entry in input) {
            if (entry.value in data.values)
                count++
        }
        return count.toDouble() / input.size
    }

}