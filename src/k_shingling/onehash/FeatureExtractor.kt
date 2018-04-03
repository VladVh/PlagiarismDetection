package k_shingling.onehash

import stopwords.Word
import java.io.File
import java.util.TreeSet

object FeatureExtractor {
    private val stopWords:List<String> = File("stopwords.txt")
            .readText().split("\r\n")
    val delimiters = ",.!?%^*()0123456789"
    val subSetLength = 5000

    fun extractWordFeatures(text : List<String>): Pair<Int, HashMap<String, Int>> {
        var totalWords = 0
        var wordCount = HashMap<String, Int>()
        for (line: String in text) {
            var replaced = line.toLowerCase().replace(Regex("[${delimiters}]"), " ")
            replaced = replaced.trim().replace(Regex(" +"), " ")
            var words = replaced.split(" ").filter { s -> !s.isEmpty() }
            totalWords += words.count()

            for (word: String in words)
            {
                var result = wordCount.putIfAbsent(word, 1)
                if (result != null)
                    wordCount[word] = wordCount.getValue(word) + 1
            }
        }
        return Pair(totalWords, wordCount)
    }

    fun extractWords(document: String): ArrayList<Word> {
//        println(document.substring(159373, 159450))
//        println(document.substring(87129, 87300))
//        println(document.substring(239444, 239650))
        val foundWords = ArrayList<Word>()
        var word = StringBuilder()
        for ((counter, character) in document.toLowerCase().toCharArray().withIndex())
        {
            if (character.isLetter())
                word.append(character)
            else if ((character.toInt() == 39 || character.toInt() == 45) && word.length > 1)
                word.append(character)
            else if (!word.isEmpty()) {
                if (word.toString() !in stopWords) {
                    foundWords.add(Word(word.toString(), counter - word.length, counter))
                }
                word.delete(0, word.length)
            }
        }
        return foundWords
    }

    fun extractWordsSets(document: String): ArrayList<ArrayList<Word>> {
        var wordSets = ArrayList<ArrayList<Word>>()
        var characters = document
        var offset = 0
        while (characters.isNotEmpty()) {
            val foundWords = ArrayList<Word>()
            var word = StringBuilder()
            var nextIteration = false
            for ((counter, character) in characters.toLowerCase().toCharArray().withIndex())
            {
                if (character.isLetter())
                    word.append(character)
                else if ((character.toInt() == 39 || character.toInt() == 45) && word.length > 1)
                    word.append(character)
                else if (!word.isEmpty()) {
                    if (word.toString() !in stopWords) {
                        foundWords.add(Word(word.toString(), offset + counter - word.length, offset + counter))
                    }
                    word.delete(0, word.length)
                    if (counter >= subSetLength) {
                        characters = characters.drop(counter)
                        offset += counter
                        nextIteration = true
                        wordSets.add(foundWords)
                        break
                    }
                }
            }
            if (!nextIteration) {
                characters = ""
                wordSets.add(foundWords)
            }

        }
        return wordSets
    }


    fun getUniqueWords(document: String): Set<Word> {
        val words = extractWords(document)
        return words.toSet()
    }


    fun extractSourceNames(pairs: List<String>): Set<String> {
        var names = TreeSet<String>()
        pairs.map { item -> names.add(item.split(" ")[1]) }
        return names
    }

    fun extractSuspNames(pairs: List<String>): Set<String> {
        var names = TreeSet<String>()
        pairs.map { item -> names.add(item.split(" ")[0]) }
        return names
    }

    fun getSuspNamesMap(pairs: List<String>): Map<String, List<String>> {
        var map = HashMap<String, List<String>>()
        var list = ArrayList<String>()
        for (pair in pairs) {
            var susp = pair.split(" ")[0]
            var src = pair.split(" ")[1]
            if (!map.containsKey(susp)) {
                map[susp] = arrayListOf(src)
            } else {
                map[susp] = map[susp]?.plus(src)!!
            }
        }
        return map
    }
}