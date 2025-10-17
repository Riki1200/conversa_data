package services.ingest

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import services.embeddings.EmbeddingsService
import utils.simpleChunk

interface IngestService {
    suspend fun ingestFromGcs(gcsPath: String, titleOverride: String? = null): Int
    suspend fun ingestFromUrl(url: String, titleOverride: String? = null): Int
}


class IngestServiceImpl(
    private val http: HttpClient,
    private val embeddings: EmbeddingsService,
    private val elasticBaseUrl: String,
    private val elasticApiKey: String,
    private val indexName: String = "docs"
) : IngestService {

    override suspend fun ingestFromGcs(gcsPath: String, titleOverride: String?): Int {
        // TODO: Descarga desde GCS (con signedUrl temporal) o usa GCS client si la SA tiene acceso directo
        // Por MVP: asume que puedes leer el archivo como texto (si es CSV o txt).
        val text = "TODO: read $gcsPath"
        return processText(text, titleOverride ?: gcsPath)
    }

    override suspend fun ingestFromUrl(url: String, titleOverride: String?): Int {
        val text = http.get(url).bodyAsText()
        return processText(text, titleOverride ?: url)
    }

    private suspend fun processText(text: String, title: String): Int {
        val chunks = simpleChunk(text, maxChars = 2000)
        val vectors = embeddings.embed(chunks)

        chunks.forEachIndexed { i, chunk ->
            indexDoc(
                title = title,
                content = chunk,
                url = title, // o gcs canonical
                chunkId = "${title}#$i",
                vector = vectors[i]
            )
        }
        return chunks.size
    }

    private suspend fun indexDoc(
        title: String,
        content: String,
        url: String,
        chunkId: String,
        vector: FloatArray
    ) {
        val vec = vector.joinToString(prefix = "[", postfix = "]", separator = ",") { it.toString() }
        val body = """
        { "title": ${title.quote()},
          "content": ${content.quote()},
          "url": ${url.quote()},
          "chunk_id": ${chunkId.quote()},
          "embedding": $vec
        }
        """.trimIndent()

        http.post("$elasticBaseUrl/$indexName/_doc") {
            header(HttpHeaders.Authorization, "ApiKey $elasticApiKey")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

private fun String.quote() = "\"" + replace("\"","\\\"") + "\""