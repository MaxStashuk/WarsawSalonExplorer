package com.sumup.scraper

import com.sumup.scraper.api.*
import com.sumup.scraper.db.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun main() {
    println("Starting Warsaw Beauty Salon Scraper...")

    val apiKey = System.getenv("GOOGLE_PLACES_API_KEY")

    if(apiKey.isNullOrBlank()) {
        System.err.println("Missing required GOOGLE_PLACES_API_KEY")
        System.exit(-1)
    }

    // Initialize the database and create the table
    DatabaseFactory.init()
    val apiClient = PlacesClient(apiKey)
    println("Database initialized successfully. Schema is ready.")

    //Define Warsaw anchor grid (Approx. 2.5 km each
    val anchors = listOf(
        Pair(52.2297, 21.0122), // Śródmieście (Center)
        Pair(52.1930, 21.0280), // Mokotów
        Pair(52.2384, 20.9548), // Wola
        Pair(52.2599, 21.0345), // Praga-Północ
        Pair(52.1485, 21.0450), // Ursynów
        Pair(52.2514, 20.9126), // Bemowo
        Pair(52.2874, 20.9419), // Bielany
        Pair(52.2796, 21.0478)  // Targówek
    )

    val placeTypes = listOf("beauty_salon", "hair_care")
    val uniquePlaceIds = mutableSetOf<String>()

    // target limit, economy of the tokens
    val RAW_TARGET = 120

    //nearby search with pagination
    //searchLoop label for exiting when hit the target
    searchLoop@ for((index, anchor) in anchors.withIndex()) {
        val (lattitude, longitude) = anchor
        println("Searching anchor $index: at ($lattitude, $longitude)")

        for (type in placeTypes) {
            var pageToken: String? = null
            var pagesFetched = 0


            do {
                println("   Fetching: $type | page ${pagesFetched + 1}")
                val response = apiClient.searchNearby(lattitude, longitude, 2500, type, pageToken)
                if(response != null) {
                    // adding all the IDs of places that are not already in uniquePlacesId (remove duplicates on the edges of anchors with overlapping radiuses)
                    val initialSize = uniquePlaceIds.size
                    response.results.forEach {
                        place -> uniquePlaceIds.add(place.place_id)
                    }
                    val added = uniquePlaceIds.size - initialSize;
                    println("       Found ${response.results.size} places. Added $added new places")
                }

                //println(response?.results)

                pageToken = response?.next_page_token;
                pagesFetched++

                //early exit of the loop if we hit the goal
                if(uniquePlaceIds.size >= RAW_TARGET) {
                    println("Target amount of places was scraped.")
                    break@searchLoop
                }

                // Added a timeout according to Google Places API. it requires a short delay, before next_page_token becomes valid
                // Imediate request leads to the INVALID_REQUEST error.
                if(pageToken != null) {
                    Thread.sleep(2000)
                }

            } while (pageToken != null && pagesFetched < 2)
        }
    }


    println("---------------------------------------\nStarting the details fetching")
    var savedCount = 0

    // Opening database transaction
    transaction {
        for((index, placeId) in uniquePlaceIds.withIndex()) {
            // Standard API call sleep time
            Thread.sleep(100)

            val details = apiClient.getPlaceDetails(placeId)
            if(details == null) {
                System.err.println("Unable to get details of: $placeId")
                continue
            }

            val districtComponent = details.address_components.find { it.types.contains("sublocality_level_1") }
            val district = districtComponent?.long_name ?: "Unknown"
            val name = details.name
            val address = details.formatted_address

            if (address == null) {
                println("   -> Missing address, skipping.")
                continue
            }


            val servicesJson = Json.encodeToString(details.types)
            val currentTime = System.currentTimeMillis()

            // SQLite doesn't have a clean native "UPSERT" that plays nicely with Exposed's IntIdTable,
            // so we do a standard lookup-then-update/insert.
            val existingSalon = Salons.selectAll().where { Salons.placeId eq placeId }.singleOrNull()

            if (existingSalon != null) {
                Salons.update({ Salons.placeId eq placeId }) {
                    it[Salons.name] = name
                    it[Salons.address] = address
                    it[Salons.district] = district
                    it[Salons.phone] = details.formatted_phone_number
                    it[Salons.website] = details.website
                    it[Salons.services] = servicesJson
                    it[Salons.priceLevel] = details.price_level
                    it[Salons.updatedAt] = currentTime
                    it[Salons.rating] = details.rating
                    it[Salons.reviewsCount] = details.user_ratings_total
                    it[Salons.lat] = details.geometry?.location?.lat
                    it[Salons.lng] = details.geometry?.location?.lng
                }
            } else {
                Salons.insert {
                    it[Salons.placeId] = placeId
                    it[Salons.name] = name
                    it[Salons.address] = address
                    it[Salons.district] = district
                    it[Salons.phone] = details.formatted_phone_number
                    it[Salons.website] = details.website
                    it[Salons.services] = servicesJson
                    it[Salons.priceLevel] = details.price_level
                    it[Salons.updatedAt] = currentTime
                    it[Salons.rating] = details.rating
                    it[Salons.reviewsCount] = details.user_ratings_total
                    it[Salons.lat] = details.geometry?.location?.lat
                    it[Salons.lng] = details.geometry?.location?.lng
                }
            }
            savedCount++
        }
    }
    println("Saved $savedCount places to database.")
    println("Scrape complete.")
}