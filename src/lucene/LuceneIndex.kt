package lucene

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
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

    fun search(searchQuery: String) {
        indexReader = DirectoryReader.open(index)
        indexSearcher = IndexSearcher(indexReader)

        var queryParser = QueryParser(searchQuery, StandardAnalyzer())
        val query = queryParser.parse(searchQuery)
        var results = indexSearcher.search(query, 2)
        var hits = results.scoreDocs
        var doc1 = indexReader.document(results.scoreDocs[0].doc)
        var doc2 = indexReader.document(results.scoreDocs[1].doc)
        println()
    }


}