package com.sumup.scraper

import com.sumup.scraper.api.PlacesClient
import com.sumup.scraper.db.DatabaseFactory

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

                pageToken = response?.next_page_token;
                pagesFetched++

                // Added a timeout according to Google Places API. it requires a short delay, before next_page_token becomes valid
                // Imediate request leads to the INVALID_REQUEST error.
                if(pageToken != null) {
                    Thread.sleep(2000)
                }

            } while (pageToken == null && pagesFetched < 3)
        }
    }

}