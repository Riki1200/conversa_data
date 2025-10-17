package com.romeodev

import io.ktor.server.application.*



fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    //configureAdministration()
    configureFrameworks()
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()
}
