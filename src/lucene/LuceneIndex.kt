package lucene

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.*
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser
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

    fun indexDocument(data: String) {
        val document = Document()
        document.add(TextField("content", data, Field.Store.YES))
        indexWriter.addDocument(document)
        indexWriter.commit()
    }

    fun search(searchQuery: String) {
        indexReader = DirectoryReader.open(index)
        indexSearcher = IndexSearcher(indexReader)

        var queryParser = QueryParser("", StandardAnalyzer())
        val term1 =  Term("content", "mention")
        val term2 =  Term("content", "wishes")
        val term3 = Term("content", "serious")

        var phraseQuery = PhraseQuery.Builder()
        phraseQuery.add(term3)
        phraseQuery.add(term2)
        phraseQuery.add(term1)
        phraseQuery.setSlop(20) //distinct words between
        var res = indexSearcher.search(phraseQuery.build(), 5)


        val query = queryParser.parse(searchQuery)
        var results = indexSearcher.search(query, 100000)
        var hits = results.scoreDocs
        var doc1 = indexReader.document(results.scoreDocs[0].doc)
        var doc2 = indexReader.document(results.scoreDocs[1].doc)
        println()
    }

    fun search(query: Query): TopDocs? {
        indexReader = DirectoryReader.open(index)
        indexSearcher = IndexSearcher(indexReader)
        return indexSearcher.search(query, 100000)
    }

    fun checkSuspiciousDocument(words: List<Word>) {
        val instances = 3
        val iterations = 10
        val step = words.size / iterations

        if (words.size > instances * iterations) {
            var items = words.take(3)
            val query = createQuery(items)
            val result = search(query)
        }

    }
    fun createQuery (words : List<Word>):Query {
        var queryString = "${words[0].text}:${words[0].text}"
        for (i in 1 until words.size) {
            queryString = queryString.plus(" AND ${words[i].text}:${words[i].text}")
        }
        queryString = queryString.plus("~10")
        val queryParser = QueryParser("", StandardAnalyzer())
        return queryParser.parse(queryString)
    }
}