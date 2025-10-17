package routes


import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.QueryReq
import models.SearchRes
import org.koin.ktor.ext.inject
import services.embeddings.EmbeddingsService
import services.search.ElasticSearchService
import utils.rrfFuse

fun Route.searchRoutes() {
    val search by inject<ElasticSearchService>()
    val embed by inject<EmbeddingsService>()

    post("/search") {
        val req = call.receive<QueryReq>()
        val e = embed.embed(listOf(req.query)).first()
        val bm25 = search.bm25(req.query, size = 20)
        val knn  = search.knn(e, size = 20)
        val fused = rrfFuse(bm25, knn, topK = req.topK)
        call.respond(SearchRes(results = fused))
    }
}