package lucene

import k_shingling.onehash.FeatureExtractor
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.*
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*
import org.apache.lucene.search.spans.*
import org.apache.lucene.store.Directory
import org.apache.lucene.store.RAMDirectory
import stopwords.Word
import org.apache.lucene.search.spans.SpanWeight
import org.apache.lucene.search.spans.SpanTermQuery
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.store.MMapDirectory
import java.io.File
import java.nio.file.Path


//class MyFldType: FieldType() {
//    constructor() {
//        this.setIndexOptions(org.apache.lucene.index.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS)
//    }
//}

class LuceneIndex(val featureExtractor: FeatureExtractor) {
    private val analyzer: StandardAnalyzer = StandardAnalyzer()
    private val config: IndexWriterConfig
    private val index: Directory
    private val indexWriter: IndexWriter
    private lateinit var indexReader: IndexReader
    private lateinit var indexSearcher: IndexSearcher

    init {
        config = IndexWriterConfig(analyzer)
        config.openMode = IndexWriterConfig.OpenMode.APPEND
        index = MMapDirectory(File("E:\\Intellij projects\\pan13-text-alignment-training-corpus-2013-01-21\\indexing").toPath())
        indexWriter = IndexWriter(index, config)
    }

    fun indexDocument(data: List<Word>, name: String) {
        val document = Document()
        val text = data.joinToString(separator = " ")
        document.add(TextField("content", text, Field.Store.NO))
        document.add(TextField("name", name, Field.Store.YES))
        //data.forEach { item -> document.add(TextField(item.text, item.text, Field.Store.YES)) }
//        document.add(Field("content", text, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS))
//        document.add(Field("name", name, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS))

        indexWriter.addDocument(document)
        indexWriter.commit()
    }

//    fun indexDocument(data: String, name: String) {
//        val document = Document()
//        document.add(TextField("content", data, Field.Store.NO))
//        document.add(TextField("name", name, Field.Store.YES))
//        indexWriter.addDocument(document)
//        indexWriter.commit()
//    }

    fun searchTest(searchQuery: String) {
        indexReader = DirectoryReader.open(index)
        indexSearcher = IndexSearcher(indexReader)

        var spanQuery: SpanQuery = SpanTermQuery(Term("content", "find"))


        var complexQuery = SpanNearQuery(arrayOf(
                SpanTermQuery(Term("content", "find")),
                SpanTermQuery(Term("content", "council")),
                SpanTermQuery(Term("content", "met")),
                SpanTermQuery(Term("content", "unionists")),
                SpanTermQuery(Term("content", "constituted"))),
                13,
                false)
        var result = indexSearcher.search(complexQuery, 10000)

        var leaves = indexReader.leaves()
        val ctx = leaves[0]
        val spanWeight = spanQuery.createWeight(indexSearcher, false, 1.0F)
        val spans = spanWeight.getSpans(ctx, SpanWeight.Postings.POSITIONS)
        while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
            while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {
                println(spans.startPosition())
                println(spans.endPosition())
            }
        }

        //3.6.2
//        var queryParser = QueryParser("", StandardAnalyzer())
//        val term1 =  Term("content", "mention")
//        val term2 =  Term("content", "wishes")
//        val term3 = Term("content", "simmers")
        println()
    }

    fun search(words: List<Word>): TopDocs? {
        indexReader = DirectoryReader.open(index)
        indexSearcher = IndexSearcher(indexReader)

        val spanQueries = arrayListOf<SpanQuery>()

        for (word in words) {
            val synsets = featureExtractor.getWordSynsets(word)
            if (synsets != null) {
                val subqueries = arrayListOf<SpanQuery>()
                val synonyms = arrayListOf<String>()
                for (synset in synsets) {
                    synonyms.addAll(synset.words.map { it.lemma })
                }
                val unique = synonyms.distinct()
                for (str in unique) {
                    val query = SpanTermQuery(Term("content", str))
                    subqueries.add(query)
                }
                spanQueries.add(SpanOrQuery(*subqueries.toTypedArray()))

            } else {
                spanQueries.add(SpanTermQuery(Term("content", word.text)))
            }
        }
        val spanComplexQuery = SpanNearQuery(spanQueries.toTypedArray(), 80, false)
//        words.forEach { spanQueries.add(SpanTermQuery(Term("content", it.text))) }
//        val spanComplexQuery = SpanNearQuery(spanQueries..toTypedArray(), 100, false)
        return indexSearcher.search(spanComplexQuery, 100)
    }


    fun search(query: Query): TopDocs? {
        indexReader = DirectoryReader.open(index)
        indexSearcher = IndexSearcher(indexReader)
        return indexSearcher.search(query, 100000)
    }

    fun checkSuspiciousDocument(words: List<Word>): List<String> {
        var wordsList = words
        val instanceSize = 15

        val found = ArrayList<String>()
        var start = -1
        var end = 0
        var sourceDocs: List<String> = ArrayList<String>()
        while (wordsList.size > 10) {
            val paragraph: List<Word> = if (wordsList.size > instanceSize)
                wordsList.take(instanceSize)
            else {
                wordsList.subList(0, wordsList.size)
            }

            //val query = createQuery(paragraph.subList(0,7))
            val results = search(paragraph.subList(0, 9))
            //val results = search(query)
            if (results?.totalHits ?: 0 > 0) {
                var docs = extractDocsByIds(results?.scoreDocs!!)
                var intersection = emptySet<String>()
                if (sourceDocs.isNotEmpty()) {
                    intersection = sourceDocs.intersect(docs)
                }

                if (intersection.isNotEmpty()) {
                    if (end != 0 && (paragraph.last().end - end) < 400) {
                        end = paragraph.last().end
                    } else {
                        if (start != -1)
                            println("$start $end: $sourceDocs")
                        //println("${paragraph[0].start} ${paragraph.last().end}")
                        found.addAll(sourceDocs)

                        start = paragraph[0].start
                        end = paragraph.last().end
                    }
                    sourceDocs = intersection.toList()
                } else {
                    if (start != -1)
                        println("$start $end: $sourceDocs")
                    found.addAll(sourceDocs)

                    sourceDocs = docs
                    start = paragraph[0].start
                    end = paragraph.last().end

                }
//
//                if (end != 0 && (paragraph.last().end - end) < 400) {
//                    end = paragraph.last().end
//                } else {
//                    if (start != -1)
//                        println("$start $end: $sourceDocs")
//                    //println("${paragraph[0].start} ${paragraph.last().end}")
//                    start = paragraph[0].start
//                    end = paragraph.last().end
//                }
            }
            wordsList = wordsList.drop(2)
        }
        if (start != -1) {
            println("$start $end $sourceDocs")
            found.addAll(sourceDocs)
        }
        return found
    }

    fun extractDocsByIds(scoreDocs: Array<ScoreDoc>): ArrayList<String> {
        var results = ArrayList<String>()
        scoreDocs.mapTo(results) { indexReader.document(it.doc).getField("name").stringValue() }
        return results
    }

    fun createQuery(words: List<Word>): Query {
        var queryString = "${words[0].text}:${words[0].text}"
        for (i in 1 until words.size) {
            queryString = queryString.plus(" AND ${words[i].text}:${words[i].text}")
        }
        //queryString = queryString.plus(" ~30")
        val queryParser = QueryParser("", StandardAnalyzer())

        var phraseQuery = PhraseQuery.Builder()
        words.forEach { phraseQuery.add(Term("content", it.text)) }

        phraseQuery.setSlop(200) //distinct words between
        return phraseQuery.build()
    }
}