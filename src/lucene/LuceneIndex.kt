package lucene

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.*
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder
import org.apache.lucene.search.*
import org.apache.lucene.store.Directory
import org.apache.lucene.store.RAMDirectory
import stopwords.Word

class LuceneIndex {
    private val analyzer: StandardAnalyzer = StandardAnalyzer()
    private val config: IndexWriterConfig
    private val index: Directory
    private val indexWriter: IndexWriter
    private lateinit var indexReader: IndexReader
    private lateinit var indexSearcher: IndexSearcher
    init {
        config = IndexWriterConfig(analyzer)
        index = RAMDirectory()
        indexWriter = IndexWriter(index, config)
    }

    fun indexDocument(data: List<Word>) {
        val document = Document()
        data.forEach { item -> document.add(TextField(item.text, item.text, Field.Store.YES)) }
        indexWriter.addDocument(document)
        indexWriter.commit()
    }

    fun indexDocument(data: String, name: String) {
        val document = Document()
        document.add(TextField("content", data, Field.Store.YES))
        document.add(TextField("name", name, Field.Store.YES))
        indexWriter.addDocument(document)
        indexWriter.commit()
    }

    fun search(searchQuery: String) {
        indexReader = DirectoryReader.open(index)
        indexSearcher = IndexSearcher(indexReader)

        var queryParser = QueryParser("", StandardAnalyzer())
        val term1 =  Term("content", "mention")
        val term2 =  Term("content", "wishes")
        val term3 = Term("content", "simmers")

        var phraseQuery = PhraseQuery.Builder()
        phraseQuery.add(term3)
        phraseQuery.add(term2)
        phraseQuery.add(term1)

        phraseQuery.setSlop(20) //distinct words between
        var res = indexSearcher.search(phraseQuery.build(), 5)

        var booleanQuery = BooleanQuery.Builder()
        booleanQuery.add(TermQuery(term1), BooleanClause.Occur.SHOULD)

        println()
    }

    fun search(query: Query): TopDocs? {
        indexReader = DirectoryReader.open(index)
        indexSearcher = IndexSearcher(indexReader)
        return indexSearcher.search(query, 100000)
    }

    fun checkSuspiciousDocument(words: List<Word>) {
        var wordsList = words
        val instanceSize = 10
        while (wordsList.size > instanceSize) {
            val paragraph = wordsList.take(instanceSize)
            val query = createQuery(paragraph.subList(0,10))
            val results = search(query)
            if (results?.totalHits ?: 0 > 0) {
                println("${paragraph[0].start} ${paragraph.last().end}")
            }
            wordsList = wordsList.drop(instanceSize)
        }
    }
    fun createQuery (words : List<Word>):Query {
//        var queryString = "${words[0].text}:${words[0].text}"
//        for (i in 1 until words.size) {
//            queryString = queryString.plus(" AND ${words[i].text}:${words[i].text}")
//        }
//        //queryString = queryString.plus(" ~30")
//        val queryParser = QueryParser("", StandardAnalyzer())

        var phraseQuery = PhraseQuery.Builder()
        words.forEach { phraseQuery.add(Term("content", it.text)) }

        phraseQuery.setSlop(200) //distinct words between
        return phraseQuery.build()
    }
}