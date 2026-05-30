package com.sumup.backend

import com.sumup.backend.db.DatabaseFactory
import com.sumup.backend.routes.configureSalonRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Options)
    }

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    configureSalonRoutes()
}
