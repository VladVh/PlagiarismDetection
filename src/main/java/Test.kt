import k_shingling.onehash.DataSet
import k_shingling.onehash.FeatureExtractor
import lucene.LuceneIndex
import stopwords.Word
import java.io.*


const val BASE_DIR = "E:\\Intellij projects\\pan13-text-alignment-training-corpus-2013-01-21"
//const val BASE_DIR = "D:\\Навчання\\Диплом\\pan13-text-alignment-training-corpus-2013-01-21"
fun main(args : Array<String>) {
    var featureExtractor = FeatureExtractor()
//    DataSet.deserialize()
//    println(DataSet.collection.size)
    println(File("${BASE_DIR}\\serialization").exists())
    var pairs = File("$BASE_DIR\\02-no-obfuscation\\pairs")
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


//    JWNL.initialize(FileInputStream("properties.xml"))
//    val dictionary = Dictionary.getInstance()
//    val result = POS.getAllPOS()
//    val word = dictionary.getIndexWord(POS.VERB, "bend")
//    val senseCount = word.senseCount
//    for (i in 1..senseCount) {
//        println(word.getSense(i).words)
//    }


    //lucene
    var lucene = LuceneIndex(featureExtractor)
    var folder = File("$BASE_DIR\\src")

    val out = FileInputStream("$BASE_DIR\\serialization\\total.out")
        val oos = ObjectInputStream(out)
    val documents = oos.readObject() as ArrayList<List<Word>>
        oos.close()
    //val documents = ArrayList<List<Word>>()
    //var folder = File("$BASE_DIR\\test")
    for ((index,file) in folder.listFiles().withIndex()) {
//        var document = featureExtractor.getDocumentPOS(file.path)
//        document = featureExtractor.normalizeDocument(document)
//        documents.add(document)
        var document = documents[index]

        var unique = document.distinctBy { word -> word.text }
        DataSet.addWords(unique)
//        lucene.indexDocument(document, file.name)
//
//        System.gc()
        if (index % 100 == 0)
//            println()
        println(file.name)
    }
//        val out = FileOutputStream("${BASE_DIR}\\serialization\\total.out")
//        val oos = ObjectOutputStream(out)
//        oos.writeObject(documents)
//        oos.flush()
//        oos.close()
//
    DataSet.findHighIdfWords()

//    for (file in folder.listFiles()) {
//        var data = FeatureExtractor.extractWords(file.readText())
//        data.removeIf { item -> !DataSet.wordsSorted.contains(item.text) }
//
//    }

//    var words = featureExtractor.extractWords(
//            File("E:\\Intellij projects\\pan13-text-alignment-training-corpus-2013-01-21\\susp\\suspicious-document01524.txt")
//                    .readText())
//    words = featureExtractor.normalizeDocument(words)
//    //words.removeIf { !DataSet.wordsSorted.contains(it.text) }
//    lucene.checkSuspiciousDocument(words)


    var correct = 0
    var correctN = 0
    var total = 0
    var incorrect = 0
    var suspToSrcMap = featureExtractor.getSuspNamesMap(pairs.readLines())

    for (suspFile in featureExtractor.extractSuspNames(pairs.readLines())) {
        var file = File("$BASE_DIR\\susp\\$suspFile")
        println(file.name)
        var document = featureExtractor.getDocumentPOS(file.path)
        //var data = featureExtractor.extractWords(file.readText())
        document = featureExtractor.normalizeDocument(document)
        document = document.toMutableList()
        //document.removeIf { item -> !DataSet.wordsSorted.contains(item.text) }

        var totalFound = lucene.checkSuspiciousDocument(document)

        val needed = suspToSrcMap[file.name]!!
        total += needed.size
        if (totalFound.containsAll(needed))
            correct++
        else
            incorrect++
        correctN += needed.size - needed.subtract(totalFound).size
    }
    println("$correct $incorrect")
    println("$correctN $total")


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
