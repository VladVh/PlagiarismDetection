import k_shingling.onehash.DataSet
import k_shingling.onehash.Shingling
import naivebayes.FeatureExtractor
import stopwords.StopWordsPlagiarismDetector
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import java.security.MessageDigest

//val baseDir = "E:\\Intellij projects\\pan13-text-alignment-training-corpus-2013-01-21"
const val baseDir = "D:\\Навчання\\Диплом\\pan13-text-alignment-training-corpus-2013-01-21"
fun main(args : Array<String>) {
    //DataSet.deserialize()
    //println(DataSet.collection.size)

    var pairs = File("$baseDir\\02-no-obfuscation\\pairs")
    var sourceFiles = FeatureExtractor.extractSourceNames(pairs.readLines())
    for (file in sourceFiles) {
        var path = "$baseDir\\src\\$file"
        var wordSets = FeatureExtractor.extractWordsSets(File(path).readText())
        wordSets
                .map { Shingling.createShingles(it) }
                .forEachIndexed { counter, hashes -> DataSet.addItem(file + "_" + counter, hashes) }

    }
    //DataSet.serialize()
    //low obfuscation

    var document = FeatureExtractor.extractWordsSets(File("$baseDir\\susp\\suspicious-document00048.txt").readText())
    for (list in document) {
        var hashesInput = Shingling.createShingles(list)
        Shingling.compareAgainstDataset(hashesInput)
    }
    //var document2 = FeatureExtractor.extractWords(File("E:\\Intellij projects\\pan13-text-alignment-training-corpus-2013-01-21\\src\\source-document03043.txt").readText())
    //var hashesData = Shingling.createShingles(document2)
//    DataSet.addItem("source-document03751.txt", hashesData)
//    DataSet.serialize()

    //Shingling.test(document, document2)
    var dir = File("$baseDir\\susp")
    for (file in dir.listFiles()) {
        println(file.name)
        var data = FeatureExtractor.extractWords(file.readText())
        var hashes = Shingling.createShingles(data)
        Shingling.compareAgainstDataset(hashes)
    }

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
