package com.sumup.backend.routes

import com.sumup.backend.models.ErrorResponse
import com.sumup.backend.models.SalonPatchRequest
import com.sumup.backend.service.PatchResult
import com.sumup.backend.service.SalonService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

private val PATCH_ALLOWED_KEYS = setOf("name", "address", "district", "services", "priceLevel")

fun Application.configureSalonRoutes() {
    routing {
        route("/api") {
            get("/health") {
                call.respond(mapOf("status" to "ok"))
            }

            get("/salons") {
                val district = call.request.queryParameters["district"]
                val q        = call.request.queryParameters["q"]
                val sort     = call.request.queryParameters["sort"]
                val page     = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                call.respond(SalonService.getSalons(district, q, sort, page, pageSize))
            }

            get("/salons/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid id", "BAD_REQUEST")
                    )
                val salon = SalonService.getSalonById(id)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("Salon not found", "NOT_FOUND")
                    )
                call.respond(salon)
            }

            patch("/salons/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid id", "BAD_REQUEST")
                    )

                val body = call.receive<JsonObject>()
                val unknownKeys = body.keys - PATCH_ALLOWED_KEYS
                if (unknownKeys.isNotEmpty()) {
                    return@patch call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Unknown fields: ${unknownKeys.joinToString()}", "BAD_REQUEST")
                    )
                }

                val patch = Json.decodeFromJsonElement<SalonPatchRequest>(body)
                when (val result = SalonService.patchSalon(id, patch)) {
                    is PatchResult.Success         -> call.respond(result.salon)
                    is PatchResult.NotFound        -> call.respond(HttpStatusCode.NotFound, ErrorResponse("Salon not found", "NOT_FOUND"))
                    is PatchResult.ValidationError -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(result.message, "BAD_REQUEST"))
                }
            }

            get("/districts") {
                call.respond(SalonService.getDistricts())
            }
        }
    }
}
