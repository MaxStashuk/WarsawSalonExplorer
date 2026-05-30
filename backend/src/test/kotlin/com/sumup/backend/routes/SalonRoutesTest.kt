package com.sumup.backend.routes

import com.sumup.backend.db.Salons
import com.sumup.backend.models.SalonDetail
import com.sumup.backend.models.SalonListResponse
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals

class SalonRoutesTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun freshDb() {
        val tmp = java.io.File.createTempFile("salon_test_", ".db").also { it.deleteOnExit() }
        Database.connect(url = "jdbc:sqlite:${tmp.absolutePath}", driver = "org.sqlite.JDBC")
        transaction { SchemaUtils.create(Salons) }
    }

    private fun insertSalon(name: String = "Test Salon", district: String = "Mokotów"): Int = transaction {
        Salons.insertAndGetId { stmt ->
            stmt[Salons.placeId]    = "place-${System.nanoTime()}"
            stmt[Salons.name]       = name
            stmt[Salons.address]    = "ul. Testowa 1, Warsaw"
            stmt[Salons.district]   = district
            stmt[Salons.rating]     = 4.5
            stmt[Salons.priceLevel] = 2
            stmt[Salons.updatedAt]  = System.currentTimeMillis()
        }.value
    }

    private fun withApp(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        install(ContentNegotiation) { json(json) }
        application { configureSalonRoutes() }
        block()
    }

    @Test
    fun `GET salons returns 200 with items and total`() {
        freshDb()
        insertSalon()
        withApp {
            val response = client.get("/api/salons")
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.decodeFromString<SalonListResponse>(response.bodyAsText())
            assertEquals(1, body.total)
            assertEquals("Test Salon", body.items[0].name)
        }
    }

    @Test
    fun `GET salon by id returns 404 for missing salon`() {
        freshDb()
        withApp {
            val response = client.get("/api/salons/999")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }

    @Test
    fun `PATCH salon updates field and returns full record`() {
        freshDb()
        val id = insertSalon()
        withApp {
            val response = client.patch("/api/salons/$id") {
                contentType(ContentType.Application.Json)
                setBody("""{"name":"Renamed Salon"}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.decodeFromString<SalonDetail>(response.bodyAsText())
            assertEquals("Renamed Salon", body.name)
        }
    }

    @Test
    fun `PATCH with unknown field returns 400`() {
        freshDb()
        val id = insertSalon()
        withApp {
            val response = client.patch("/api/salons/$id") {
                contentType(ContentType.Application.Json)
                setBody("""{"rating": 5.0}""")
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }
}
