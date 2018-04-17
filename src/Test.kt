import k_shingling.onehash.DataSet
import k_shingling.onehash.Shingling
import k_shingling.onehash.FeatureExtractor
import lucene.LuceneIndex
import net.didion.jwnl.JWNL
import net.didion.jwnl.data.IndexWord
import net.didion.jwnl.data.POS
import net.didion.jwnl.dictionary.Dictionary
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.QueryBuilder
import stopwords.Word
import java.io.File

const val BASE_DIR = "E:\\Intellij projects\\pan13-text-alignment-training-corpus-2013-01-21"
//const val BASE_DIR = "D:\\Навчання\\Диплом\\pan13-text-alignment-training-corpus-2013-01-21"
fun main(args : Array<String>) {

//    DataSet.deserialize()
//    println(DataSet.collection.size)
//    var pairs = File("$BASE_DIR\\02-no-obfuscation\\pairs")
//    var sourceFiles = FeatureExtractor.extractSourceNames(pairs.readLines())
//    for (file in sourceFiles) {
//        var path = "$BASE_DIR\\src\\$file"
//        var data = File(path).readText()
//
//        var wordSets = FeatureExtractor.extractWordsSets(data)
//        wordSets
//                .map { Shingling.createShingles(it) }
//                .forEachIndexed { counter, hashes -> DataSet.addItem(file + "_" + counter, hashes) }
//
//    }
//    DataSet.serialize()
//
//    var correct = 0
//    var correctN = 0
//    var total = 0
//    var incorrect = 0
//    var suspToSrcMap = FeatureExtractor.getSuspNamesMap(pairs.readLines())
//
//    for (suspFile in FeatureExtractor.extractSuspNames(pairs.readLines())) {
//        var file = File("$BASE_DIR\\susp\\$suspFile")
//        var document = FeatureExtractor.extractWordsSets(file.readText())
//        var totalFound = ArrayList<String>()
//        for (list in document) {
//            var hashesInput = Shingling.createShingles(list)
//            totalFound.addAll(Shingling.compareAgainstDataset(hashesInput))
//        }
//        val needed = suspToSrcMap[file.name]!!
//        total += needed.size
//        if (totalFound.containsAll(needed))
//            correct++
//        else
//            incorrect++
//        correctN += needed.size - needed.subtract(totalFound).size
//    }
//    println("$correct $incorrect")


    //lucene
    var lucene = LuceneIndex()
    var folder = File("$BASE_DIR\\src")
    //var folder = File("$BASE_DIR\\test")
    for (file in folder.listFiles()) {
        var uniqueWords = FeatureExtractor.getUniqueWords(file.readText())
        DataSet.addWords(uniqueWords)

    }
    DataSet.findHighIdfWords()

    for (file in folder.listFiles()) {
        var data = FeatureExtractor.extractWords(file.readText())
        data.removeIf { item -> !DataSet.wordsSorted.contains(item.text) }
        lucene.indexDocument(file.readText(), file.name)
    }

    var words = FeatureExtractor.extractWords(
            File("E:\\Intellij projects\\pan13-text-alignment-training-corpus-2013-01-21\\susp\\suspicious-document00005.txt")
                    .readText())
    words.removeIf { !DataSet.wordsSorted.contains(it.text) }
    lucene.checkSuspiciousDocument(words)
    //lucene.search("+wonders serious")

    var word1 = Word("wonders", 1, 2)
    var word2 = Word("mention", 1, 2)
    var word3 = Word("while", 1, 2)
    var word4 = Word("worth", 1, 2)
    var word5 = Word("he", 1, 2)
    var word6 = Word("happens", 1, 2)
    var word7 = Word("be", 1, 2)
    var word8 = Word("wishes", 1, 2)
    var word9 = Word("serious", 1, 2)
    var word10 = Word("legitimate", 1, 2)
    var query2 = lucene.createQuery(arrayListOf(word1, word2, word3, word4, word5, word6, word8, word9, word10))
    var result = lucene.search(query2)

//    JWNL.initialize(null)
//    val dictionary = Dictionary.getInstance()
//    val word = dictionary.getIndexWord(POS.ADJECTIVE, "bend")
//    val senseCount = word.senseCount
//    for (i in 1..senseCount) {
//        println(word.getSense(i).words)
//    }

    println(result)
    //println(Shingling.compareShingles(hashesInput, hashesData))


    // stop words plagiarism
    /*
    var files = File("D:\\Навчання\\Диплом\\pan12-text-alignment-training-corpus-2012-03-16\\02_no_obfuscation") //03_artificial_low")
            .listFiles()

    var parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    for (file in files) {
        if (file.extension == "xml") {
            var document = parser.parse(file)
            var element = document.getElementsByTagName("document").item(0)
            var attributes = element.attributes

            val suspicious = attributes.getNamedItem("reference").nodeValue
            var source: String = "empty"
            var length: Int = 0
            var srcOffset: Int
            var thisOffset: Int

            var subElems = element.childNodes
            val subElemsCount = subElems.length
            var i = 0
            while (i < subElemsCount) {
                val subElem = subElems.item(i++)
                if (subElem.attributes?.getNamedItem("name")?.nodeValue == "plagiarism") {
                    length++
                    source = subElem.attributes.getNamedItem("source_reference").nodeValue
                }

            }
            var ngramsSusp = StopWordsPlagiarismDetector.extractNGrams(
                    File("D:\\Навчання\\Диплом\\pan12-text-alignment-training-corpus-2012-03-16\\susp\\$suspicious")
                            .readText())
            var ngrams = StopWordsPlagiarismDetector.extractNGrams(
                    File("D:\\Навчання\\Диплом\\pan12-text-alignment-training-corpus-2012-03-16\\src\\$source")
                            .readText())
            if (StopWordsPlagiarismDetector.checkDocumentForSimilarity(ngrams, ngramsSusp)) {
                var found = StopWordsPlagiarismDetector.detectPassageBoundary(ngrams, ngramsSusp)
                println("Found ${found.size} passages in $length")
            }
        }
    } */

    }
