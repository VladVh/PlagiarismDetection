import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

object SourceSuspiciousDocumentExtractor {
    private const val srcFolder:String = "D:\\Навчання\\Диплом\\pan12-text-alignment-training-corpus-2012-03-16\\src"
    private const val outSrcFolder = "D:\\Навчання\\Диплом\\pan12-text-alignment-training-corpus-2012-03-16\\srcTrimmed"

    private const val suspFolder = "D:\\Навчання\\Диплом\\pan12-text-alignment-training-corpus-2012-03-16\\susp"
    private const val outSuspFolder = "D:\\Навчання\\Диплом\\pan12-text-alignment-training-corpus-2012-03-16\\suspTrimmed"

    fun extractSourceDocuments() {
        var files = File("D:\\Навчання\\Диплом\\pan12-text-alignment-training-corpus-2012-03-16\\03_artificial_low")
                .listFiles()

        var parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        for (file in files) {
            if (file.extension == "xml")
            {
                var document = parser.parse(file)
                var element = document.getElementsByTagName("document").item(0)
                var attributes = element.attributes

                val suspicious = attributes.getNamedItem("reference").nodeValue
                var source: String
                var length: Int
                var srcOffset: Int
                var thisOffset: Int

                var subElems = element.childNodes
                val subElemsCount = subElems.length
                var i = 0
                while (i < subElemsCount) {
                    val subElem = subElems.item(i++)
                    if (subElem.attributes?.getNamedItem("name")?.nodeValue == "plagiarism")
                    {
                        length = subElem.attributes.getNamedItem("this_length").nodeValue.toInt()
                        if (length < 500)
                            continue
                        source = subElem.attributes.getNamedItem("source_reference").nodeValue
                        srcOffset = subElem.attributes.getNamedItem("source_offset").nodeValue.toInt()
                        thisOffset = subElem.attributes.getNamedItem("this_offset").nodeValue.toInt()

                        var content = File(srcFolder + "\\" + source).readText().drop(srcOffset).take(length)
                        var newFile = File(outSrcFolder + "\\" + length + source)
                        if (newFile.createNewFile())
                            newFile.writeText(content)

                        content = File(suspFolder + "\\" + suspicious).readText().drop(thisOffset).take(length)
                        newFile = File(outSuspFolder + "\\" + length + suspicious)
                        if (newFile.createNewFile())
                            newFile.writeText(content)
                    }
                }
            }
        }
    }
}