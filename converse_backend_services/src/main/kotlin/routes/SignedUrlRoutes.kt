package routes


import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.SignedUrlReq
import org.koin.ktor.ext.inject
import services.storage.StorageService

fun Route.signedUrlRoutes() {
    val storage by inject<StorageService>()

    post("/signedUrl") {
        val req = call.receive<SignedUrlReq>()
        val res = storage.createV4SignedUrl(req)
        call.respond(res)
    }
}