package routes

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import models.AnswerRes
import models.Citation
import models.QueryReq
import org.koin.ktor.ext.inject
import services.embeddings.EmbeddingsService
import services.llm.LlmService
import services.search.ElasticSearchService
import utils.rrfFuse

fun Route.answerRoutes() {
    val search by inject<ElasticSearchService>()
    val embed by inject<EmbeddingsService>()
    val llm by inject<LlmService>()

    post("/answer") {
        val req = call.receive<QueryReq>()
        val e = embed.embed(listOf(req.query)).first()
        val bm25 = search.bm25(req.query, size = 20)
        val knn  = search.knn(e, size = 20)
        val fused = rrfFuse(bm25, knn, topK = req.topK)

        val citations = fused.map { Citation(title = it.title, url = it.url, snippet = it.snippet) }
        val answer = llm.answerWithCitations(req.query, citations)
        call.respond(AnswerRes(answer = answer, citations = citations.take(req.topK)))
    }
}