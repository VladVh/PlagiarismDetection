package k_shingling.onehash

import edu.stanford.nlp.ling.TaggedWord
import edu.stanford.nlp.tagger.maxent.MaxentTagger
import edu.stanford.nlp.tagger.maxent.MaxentTaggerGUI
import edu.stanford.nlp.tagger.maxent.MaxentTaggerServer
import net.didion.jwnl.JWNL
import net.didion.jwnl.data.IndexWord
import net.didion.jwnl.data.POS
import net.didion.jwnl.data.Synset
import net.didion.jwnl.dictionary.Dictionary
import stopwords.Word
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.util.TreeSet
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

class FeatureExtractor {

    init {
        JWNL.initialize(FileInputStream("properties.xml"))
    }

    private val stopWords: List<String> = File("stopwords.txt")
            .readText().split("\r\n")
    val delimiters = ",.!?%^*()0123456789"
    private val subSetLength = 5000
    private val dictionary = Dictionary.getInstance()
    private var tagger = MaxentTagger("english-bidirectional-distsim.tagger")
    var counter = 0


    fun getDocumentPOS(path: String): List<Word> {
        val words = ArrayList<Word>()
        val reader = BufferedReader(FileReader(path))
        val tokenizedText = MaxentTagger.tokenizeText(reader)

        counter++
        if (counter == 100)
            tagger = MaxentTagger("english-bidirectional-distsim.tagger")
        for (sentence in tokenizedText) {
            if (sentence.size < 50) {
                val taggedWords = tagger.tagSentence(sentence)
                words.addAll(getWordsWithPos(taggedWords))
            }

        }
//        tokenizedText
//                .asSequence()
//                .map { tagger.tagSentence(it) }
//                .forEach { words.addAll(getWordsWithPos(it)) }
        return words
    }

    private fun getWordsWithPos(taggedWords: List<TaggedWord>): List<Word> {
        val words = ArrayList<Word>()
        taggedWords
                .asSequence()
                .filterNot { stopWords.contains(it.word().toLowerCase()) || it.word().length < 2 || it.word().toIntOrNull() != null}
                .mapTo(words) { Word(it.word(), it.beginPosition(), it.endPosition(), it.tag()) }
        return words
    }

    fun getWordSynsets(word: Word): Array<out Synset>? {
        return when {
            word.tag.contains("VB") -> dictionary.getIndexWord(POS.VERB, word.text).senses
            word.tag.contains("RB") -> dictionary.getIndexWord(POS.ADVERB, word.text).senses
            word.tag.contains("JJ") -> dictionary.getIndexWord(POS.ADJECTIVE, word.text).senses
            word.tag.contains("NN") -> dictionary.getIndexWord(POS.NOUN, word.text).senses
            else -> {
                return null
            }
        }

    }

    fun extractWordFeatures(text: List<String>): Pair<Int, HashMap<String, Int>> {
        var totalWords = 0
        var wordCount = HashMap<String, Int>()
        for (line: String in text) {
            var replaced = line.toLowerCase().replace(Regex("[${delimiters}]"), " ")
            replaced = replaced.trim().replace(Regex(" +"), " ")
            var words = replaced.split(" ").filter { s -> !s.isEmpty() }
            totalWords += words.count()

            for (word: String in words) {
                val result = wordCount.putIfAbsent(word, 1)
                if (result != null)
                    wordCount[word] = wordCount.getValue(word) + 1
            }
        }
        return Pair(totalWords, wordCount)
    }


    fun normalizeDocument(words: List<Word>): List<Word> {
        var changedWords = words
        for (word in changedWords) {
            if (word.text.length < 30) {
                var baseForms: IndexWord? = null
                when {
                    word.tag.contains("VB") -> baseForms = dictionary.morphologicalProcessor.lookupBaseForm(POS.VERB, word.text)
                    word.tag.contains("RB") -> baseForms = dictionary.morphologicalProcessor.lookupBaseForm(POS.ADVERB, word.text)
                    word.tag.contains("JJ") -> baseForms = dictionary.morphologicalProcessor.lookupBaseForm(POS.ADJECTIVE, word.text)
                    word.tag.contains("NN") -> baseForms = dictionary.morphologicalProcessor.lookupBaseForm(POS.NOUN, word.text)
                }
                if (baseForms != null) {
                    word.text = baseForms.lemma
                    baseForms = null
                }
            }
        }
        return changedWords
    }


    fun extractWords(document: String): List<Word> {
//        println(document.substring(159373, 159450))
//        println(document.substring(87129, 87300))
//        println(document.substring(239444, 239650))
        val foundWords = ArrayList<Word>()
        var word = StringBuilder()
        for ((counter, character) in document.toLowerCase().toCharArray().withIndex()) {
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
            for ((counter, character) in characters.toLowerCase().toCharArray().withIndex()) {
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