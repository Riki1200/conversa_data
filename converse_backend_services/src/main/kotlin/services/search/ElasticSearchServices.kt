package services.search

import com.romeodev.AppClients
import io.ktor.client.HttpClient

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.quote
import models.Hit
import kotlinx.serialization.Serializable


interface ElasticSearchService {
    suspend fun bm25(query: String, size: Int = 20): List<Hit>
    suspend fun knn(embedding: FloatArray, size: Int = 20): List<Hit>
    // suspend fun knn(embedding: FloatArray): ElasticHits
    // suspend fun hybrid(query: String, embedding: FloatArray): ElasticHits
}

class ElasticSearchServiceImpl(
    private val http: HttpClient,
    private val baseUrl: String,          // https://...elastic.cloud:443
    private val apiKey: String,
    private val indexName: String = "docs"
) : ElasticSearchService {

    override suspend fun bm25(query: String, size: Int): List<Hit> {
        val body = """
        {
          "size": $size,
          "query": { "multi_match": { "query": ${query.quote()}, "fields": ["title^2","content"] } },
          "highlight": { "fields": { "content": {} } }
        }
        """.trimIndent()

        val res: ElasticSearchResponse = http.post("$baseUrl/$indexName/_search") {
            header(HttpHeaders.Authorization, "ApiKey $apiKey")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

        return res.hits.hits.map { h ->
            Hit(
                title = h.source.title ?: "",
                snippet = h.highlight?.content?.firstOrNull() ?: h.source.content.take(200),
                url = h.source.url ?: "",
                score = h.score ?: 0.0
            )
        }
    }

    override suspend fun knn(embedding: FloatArray, size: Int): List<Hit> {
        // Ajusta el campo 'embedding' a tu mapping
        val vec = embedding.joinToString(prefix = "[", postfix = "]", separator = ",") { it.toString() }
        val body = """
        {
          "size": $size,
          "knn": {
            "field": "embedding",
            "query_vector": $vec,
            "k": $size,
            "num_candidates": ${size * 4}
          }
        }
        """.trimIndent()

        val res: ElasticSearchResponse = http.post("$baseUrl/$indexName/_search") {
            header(HttpHeaders.Authorization, "ApiKey $apiKey")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()

        return res.hits.hits.map { h ->
            Hit(
                title = h.source.title ?: "",
                snippet = h.source.content.take(200),
                url = h.source.url ?: "",
                score = h.score ?: 0.0
            )
        }
    }


    @Serializable data class ElasticSearchResponse(val hits: Hits)
    @Serializable data class Hits(val hits: List<HitItem>)
    @Serializable data class HitItem(
        val _score: Double? = null,
        val _source: Source,
        val highlight: Highlight? = null
    ) { val score get() = _score; val source get() = _source }
    @Serializable data class Source(val title: String? = null, val content: String, val url: String? = null)
    @Serializable data class Highlight(val content: List<String>? = null)
}

private fun String.quote() = "\"" + replace("\"","\\\"") + "\""