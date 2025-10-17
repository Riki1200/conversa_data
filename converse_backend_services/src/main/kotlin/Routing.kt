package com.romeodev


import io.ktor.server.application.*

import io.ktor.server.routing.*

import org.koin.ktor.ext.inject
import routes.*

fun Application.configureRouting() {

    routing {
        healthRoutes()
        signedUrlRoutes()
        ingestRoutes()
        searchRoutes()
        answerRoutes()
    }
}

