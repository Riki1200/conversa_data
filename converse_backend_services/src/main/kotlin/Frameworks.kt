package com.romeodev

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.codahale.metrics.*
import com.kborowy.authprovider.firebase.firebase
import com.mongodb.kotlin.client.coroutine.MongoClient
import dev.inmo.krontab.builder.*
import di.appModule
import io.github.flaxoos.ktor.server.plugins.taskscheduling.*
import io.github.flaxoos.ktor.server.plugins.taskscheduling.managers.lock.database.*
import io.github.flaxoos.ktor.server.plugins.taskscheduling.managers.lock.redis.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.metrics.dropwizard.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


import io.github.cdimascio.dotenv.dotenv

private val dotenv = dotenv()

data class AppClients(
    val http: HttpClient,
    val elasticUrl: String,
    val elasticApiKey: String,
    val vertexProject: String,
    val vertexLocation: String,
    val gcsBucket: String
)

lateinit var clients: AppClients


fun Application.configureFrameworks() {

    val elasticUrl = env("ELASTIC_URL")
    val elasticApiKey = env("ELASTIC_API_KEY")
    val vertexProject = env("VERTEX_PROJECT_ID")
    val vertexLocation = env("VERTEX_LOCATION")
    val gcsBucket = env("GCS_BUCKET")


    val tokenProvider: suspend () -> String = {
        // metadata server
        val url = "http://metadata/computeMetadata/v1/instance/service-accounts/default/token"
        val client = HttpClient() // temporal y simple
        val tokenJson: String = client.get(url) {
            header("Metadata-Flavor", "Google")
        }.bodyAsText()
        client.close()
        // tokenJson = {"access_token":"..","expires_in":..,"token_type":"Bearer"}
        // Extrae access_token rápido:
        val key = "\"access_token\":\""
        val start = tokenJson.indexOf(key) + key.length
        val end = tokenJson.indexOf('"', start)
        tokenJson.substring(start, end)
    }

    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single {
                    HttpClient(Apache) {
                        engine {
                            socketTimeout = 10_000
                            connectTimeout = 10_000
                            connectionRequestTimeout = 10_000
                        }
                        expectSuccess = true
                    }
                }
                single {
                    AppClients(
                        http = get(),
                        elasticUrl = elasticUrl,
                        elasticApiKey = elasticApiKey,
                        vertexProject = vertexProject,
                        vertexLocation = vertexLocation,
                        gcsBucket = gcsBucket
                    )
                }

                appModule(
                    elasticUrl = elasticUrl,
                    elasticApiKey = elasticApiKey,
                    vertexProject = vertexProject,
                    vertexLocation = vertexLocation,
                    gcsBucket = gcsBucket,
                    accessTokenProvider = tokenProvider
                )
            }

        )


    }
    environment.monitor.subscribe(ApplicationStopped) {
        val http: HttpClient = clients.http
        http.close()
    }
}

private fun env(name: String): String =
    System.getenv(name) ?: dotenv[name]  ?: error("Missing env var: $name")
