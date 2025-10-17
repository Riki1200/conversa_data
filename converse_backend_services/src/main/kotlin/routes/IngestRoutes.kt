package routes

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import jdk.internal.vm.ScopedValueContainer.call
import models.IngestReq
import models.IngestRes
import org.koin.ktor.ext.inject

import services.ingest.IngestService

fun Route.ingestRoutes() {
    val ingest by inject<IngestService>()

    post("/ingest") {
        val req = call.receive<IngestReq>()
        val count = when {
            req.gcsPath != null -> ingest.ingestFromGcs(req.gcsPath, req.title)
            req.fileUrl != null -> ingest.ingestFromUrl(req.fileUrl, req.title)
            else -> 0
        }
        call.respond(IngestRes(indexed = count))
    }
}