package solr

import k_shingling.onehash.FeatureExtractor
import org.apache.lucene.queries.function.valuesource.MultiValuedIntFieldSource
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.request.schema.SchemaRequest
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.MapSolrParams
import org.apache.solr.common.params.SolrParams
import stopwords.Word
import java.io.File


class SolrjIndex {
    var client = HttpSolrClient.Builder("http://localhost:8983/solr/collection1").build()

    fun clearCollection() {
        var query = SolrQuery()
        query.setQuery("*:*")
        var results = client.query(query)
        for (document in results.results) {
            client.deleteById(document.getFieldValue("id").toString())
        }
        client.commit()
    }

    fun indexFolder(folderPath: String) {
        for (file in File(folderPath).listFiles()) {
            //indexDocument(file.readText(), file.name)
            indexDocument(FeatureExtractor.extractWords(file.readText()), file.name)
        }
        client.commit()
    }

    fun indexDocument(data: String, name: String) {
        val document = SolrInputDocument()
        document.addField("content", data)
        document.addField("name", name)
        client.add(document)
        client.commit()
    }

    fun indexDocument(words: List<Word>, name: String) {
        val document = SolrInputDocument()
        document.addField("name", name)
        for (word in words) {
            document.addField(word.text, word.start)
            var f = MultiValuedIntFieldSource("abb", null)
        }
        client.add(document)
    }

    fun search()  {

        var map = HashMap<String, String>()
        map.put("fl", "wonders")

        var query = SolrQuery()
        //query.setQuery("content:simmers")
        query.addField("wonders")

        println(client.query(SolrQuery().setQuery("*:*")).results.count())

        var params = MapSolrParams(map)
        var results = client.query(query)
        var list = results?.results
        println(list?.start)
        println(list?.count())
    }
}