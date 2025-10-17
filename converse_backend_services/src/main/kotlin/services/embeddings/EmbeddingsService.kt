package services.embeddings

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

interface EmbeddingsService {
    suspend fun embed(texts: List<String>): List<FloatArray>
    val dims: Int
}


class VertexEmbeddingsService(
    private val http: HttpClient,
    private val projectId: String,
    private val location: String,
    private val accessTokenProvider: suspend () -> String,
    override val dims: Int
) : EmbeddingsService {

    private val model = "text-embedding-004"

    override suspend fun embed(texts: List<String>): List<FloatArray> {
        val token = accessTokenProvider()
        val url = "https://$location-aiplatform.googleapis.com/v1/projects/$projectId/locations/$location/publishers/google/models/$model:predict"

        val req = PredictReq(instances = texts.map { EmbeddingInstance(it) })
        val resp: PredictRes = http.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

        return resp.predictions.map { pred ->
            pred.embeddings.values.toFloatArray()
        }
    }

    @Serializable data class EmbeddingInstance(val content: String)
    @Serializable data class PredictReq(val instances: List<EmbeddingInstance>)
    @Serializable data class PredictRes(val predictions: List<Prediction>)
    @Serializable data class Prediction(val embeddings: Embeddings)
    @Serializable data class Embeddings(val values: List<Double>)
}

private fun List<Double>.toFloatArray(): FloatArray = FloatArray(size) { this[it].toFloat() }