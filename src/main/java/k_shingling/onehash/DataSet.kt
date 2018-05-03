package k_shingling.onehash

import stopwords.NGram
import stopwords.Word
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.TreeSet
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set
import kotlin.math.log2


object DataSet {
    var collection = LinkedHashMap<String, Map<NGram, String>>()
    var wordsFreq = HashMap<String, Int>()
    var wordsSorted = TreeSet<String>()
    val idfThreshold = 5

    fun addItem(name: String, shingling: Map<NGram, String>) {
        collection.putIfAbsent(name, shingling)
    }

    fun addWords(words: List<Word>) {
        for (word in words) {
            if (wordsFreq.containsKey(word.text))
                wordsFreq[word.text] = wordsFreq[word.text]!! + 1
            else
                wordsFreq[word.text] = 1
        }
    }

    private fun computeIdf(freq: Int) = log2(wordsFreq.size.toDouble() / freq)

    private fun computeTf(freq: Int, size:Int) = freq.toDouble() / size

    private fun computeTfIdf(freqInDoc: Int, totalFreq:Int, docSize:Int):Double {
        return computeTf(freqInDoc, docSize) * computeIdf(totalFreq)
    }

    fun findHighIdfWords() {
        for ((key, value) in wordsFreq) {
            if (computeIdf(value) > idfThreshold)
                wordsSorted.add(key)
        }
    }

    fun serialize() {
        try {
            val fos = FileOutputStream("fromPairs")
            val oos = ObjectOutputStream(fos)
            oos.writeObject(collection)
            oos.close()
            fos.close()
        } catch (ex: Exception) {
            println(ex)
        }

    }

    fun deserialize() {
        try {
            val fis = FileInputStream("fromPairs")
            val ois = ObjectInputStream(fis)
            collection = ois.readObject() as LinkedHashMap<String, Map<NGram, String>>
            ois.close()
            fis.close()
        } catch (ex: Exception) {
            println(ex)
        }

    }
}