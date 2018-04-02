import k_shingling.onehash.DataSet
import k_shingling.onehash.Shingling
import k_shingling.onehash.FeatureExtractor
import java.io.File

const val BASE_DIR = "E:\\Intellij projects\\pan13-text-alignment-training-corpus-2013-01-21"
//const val BASE_DIR = "D:\\Навчання\\Диплом\\pan13-text-alignment-training-corpus-2013-01-21"
fun main(args : Array<String>) {
//    DataSet.deserialize()
//    println(DataSet.collection.size)

    var pairs = File("$BASE_DIR\\02-no-obfuscation\\pairs")
    var sourceFiles = FeatureExtractor.extractSourceNames(pairs.readLines())
    for (file in sourceFiles) {
        var path = "$BASE_DIR\\src\\$file"
        var data = File(path).readText()

        var uniqueWords = FeatureExtractor.getUniqueWords(data)
        DataSet.addWords(uniqueWords)

        var wordSets = FeatureExtractor.extractWordsSets(data)
        wordSets
                .map { Shingling.createShingles(it) }
                .forEachIndexed { counter, hashes -> DataSet.addItem(file + "_" + counter, hashes) }

    }
    DataSet.findHighIdfWords()
    DataSet.serialize()

    var suspToSrcMap = FeatureExtractor.getSuspNamesMap(pairs.readLines())
    //low obfuscation
    var file = File("$BASE_DIR\\susp\\suspicious-document00048.txt")
    var document = FeatureExtractor.extractWordsSets(file.readText())
    var totalFound = ArrayList<String>()
    for (list in document) {
        var hashesInput = Shingling.createShingles(list)
        totalFound.addAll(Shingling.compareAgainstDataset(hashesInput))
    }
    //var document2 = FeatureExtractor.extractWords(File("E:\\Intellij projects\\pan13-text-alignment-training-corpus-2013-01-21\\src\\source-document03043.txt").readText())
    //var hashesData = Shingling.createShingles(document2)
//    DataSet.addItem("source-document03751.txt", hashesData)
//    DataSet.serialize()

    //Shingling.test(document, document2)
    var correct = 0
    var correctN = 0
    var total = 0
    var incorrect = 0
    for (suspFile in FeatureExtractor.extractSuspNames(pairs.readLines())) {
        var file = File("$BASE_DIR\\susp\\$suspFile")
        var document = FeatureExtractor.extractWordsSets(file.readText())
        var totalFound = ArrayList<String>()
        for (list in document) {
            var hashesInput = Shingling.createShingles(list)
            totalFound.addAll(Shingling.compareAgainstDataset(hashesInput))
        }
        val needed = suspToSrcMap[file.name]!!
        total += needed.size
        if (totalFound.containsAll(needed))
            correct++
        else
            incorrect++
        correctN += needed.size - needed.subtract(totalFound).size
    }
    println("$correct $incorrect")

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
