package services.llm

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.quote
import models.Citation
import kotlinx.serialization.Serializable

interface LlmService {
    suspend fun answerWithCitations(query: String, context: List<Citation>): String
}


class GeminiLlmService(
    private val http: HttpClient,
    private val projectId: String,
    private val location: String,
    private val accessTokenProvider: suspend () -> String
) : LlmService {

    private val model = "gemini-1.5-flash" // para MVP

    override suspend fun answerWithCitations(query: String, context: List<Citation>): String {
        val token = accessTokenProvider()
        val url = "https://$location-aiplatform.googleapis.com/v1/projects/$projectId/locations/$location/publishers/google/models/$model:generateContent"

        val contextText = buildString {
            appendLine("Use only the following context to answer. Cite sources like [1], [2], [3].")
            context.forEachIndexed { i, c ->
                appendLine("${i+1}) ${c.snippet} (source: ${c.url})")
            }
        }

        val req = GenReq(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text="$contextText\n\nQuestion: $query"))
                )
            )
        )

        val res: GenRes = http.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

        return res.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No answer."
    }

    @Serializable data class GenReq(val contents: List<Content>)
    @Serializable data class Content(val role: String, val parts: List<Part>)
    @Serializable data class Part(val text: String)
    @Serializable data class GenRes(val candidates: List<Candidate>)
    @Serializable data class Candidate(val content: Content)
}