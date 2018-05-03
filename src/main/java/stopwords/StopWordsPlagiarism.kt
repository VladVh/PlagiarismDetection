package stopwords

import java.io.File
import java.util.*
import kotlin.collections.ArrayList

object StopWordsPlagiarismDetector {
    private val stopWords:List<String> = File("stopwords.txt")
            .readText().split("\r\n")
    private val veryFrequent = arrayListOf("the", "of", "and", "a", "in", "to", "'s")

    private val delimiters = ",.!?%^*()0123456789"
    val nGramLength = 8

    fun extractNGrams(document: String): ArrayList<NGram>{
        val nGrams = ArrayList<NGram>()

        val foundStopWords = ArrayList<Word>()
        var word = StringBuilder()
        for ((counter, character) in document.toLowerCase().toCharArray().withIndex())
        {
            if (character.isLetter())
                word.append(character)
            else if (character.toInt() == 39 && word.length > 1)
                word.append(character)
            else if (!word.isEmpty()){
                if (word.toString() in stopWords) {
                    foundStopWords.add(Word(word.toString(), counter - word.length, counter))
                } else if (word.length > 1 && word.substring(word.length - 2) == "'s") {
                    var text = word.substring(word.length - 2)
                    foundStopWords.add(Word(text, counter - 2, counter))
                } else if (word.length > 2 && word.substring(word.length - 3) == "n't") {
                    var text = word.substring(word.length - 3)
                    foundStopWords.add(Word(text, counter - 3, counter))
                }
                word.delete(0, word.length)
            }
        }

        if (foundStopWords.size > nGramLength) {
            var wordQueue = ArrayDeque<Word>()
            for (i in 0..nGramLength) {
                wordQueue.add(foundStopWords[i])
            }
            nGrams.add(NGram(wordQueue.joinToString(" ", transform = {it -> it.toString()}), wordQueue.first.start, wordQueue.last.end))
            for (i in nGramLength + 1 until foundStopWords.size) {
                wordQueue.pop()
                wordQueue.add(foundStopWords[i])
                nGrams.add(NGram(wordQueue.joinToString(" ", transform = {it -> it.toString()}), wordQueue.first.start, wordQueue.last.end))
            }
        }
        return nGrams
    }

    fun checkDocumentForSimilarity(srcNGrams: ArrayList<NGram>, suspNGrams: ArrayList<NGram>): Boolean {
        for ((index, srcNGram) in srcNGrams.withIndex()) {
            suspNGrams
                    .filter { srcNGram == it
                            && it.countVeryFrequent(veryFrequent) < nGramLength - 1
                            && it.countVeryFrequentSequence(veryFrequent) < nGramLength - 2 }
                    .forEach { return true }
        }
        return false
    }


    fun detectPassageBoundary(srcNGrams: ArrayList<NGram>, suspNGrams: ArrayList<NGram>): ArrayList<Passage> {
        var matches = ArrayList<Pair<NGram, NGram>>()
        for (srcNGram in srcNGrams)
            suspNGrams
                    .filter { srcNGram == it }
                    .mapTo(matches) { Pair(it, srcNGram) }


        //println("Found matches")
        var firstCriteria = 100
        val passages = ArrayList<Passage>()

        matches.sortBy { it -> it.first.start }

        while (matches.size != 0) {
            val passage = Passage(true)
            val iterator = matches.iterator()
            while (iterator.hasNext()) {
                val pair = iterator.next()
                if (passage.findDistanceToNGram(pair) < firstCriteria) {
                    passage.addPair(pair)
                    iterator.remove()
                }
            }
            passages.add(passage)
        }

        val subPassages = ArrayList<Passage>()
        for (passage in passages) {
            var pairs = passage.getPairs()
            var subpassage = Passage(false)
            while (pairs.size != 0) {
                val iterator = pairs.iterator()
                var count = pairs.size
                while (iterator.hasNext()) {
                    val pair = iterator.next()
                    if (subpassage.findDistanceToNGram(pair) < firstCriteria) {
                        subpassage.addPair(pair)
                        iterator.remove()
                    }
                }
                if (pairs.size == count) {
                    subPassages.add(subpassage)
                    subpassage = Passage(false)
                }
            }
            subPassages.add(subpassage)
        }


//        for (passage in passages) {
//            println("${passage.getPassageStart(true)} ${passage.getPassageEnd(true)}")
//        }
        return subPassages
    }
}