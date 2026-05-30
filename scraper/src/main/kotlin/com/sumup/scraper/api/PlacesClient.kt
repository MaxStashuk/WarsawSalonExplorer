package com.sumup.scraper.api

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class PlacesClient(private val apiKey: String) {

    // OkHttp client with slight timeouts for resilience
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Configured to ignore the 90% of the Google API payload we don't need
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Executes a Nearby Search for a specific latitude/longitude and type.
     */
    fun searchNearby(lat: Double, lng: Double, radius: Int = 2500, type: String, pageToken: String? = null): NearbySearchResponse? {
        val url = buildString {
            append("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
            append("?location=$lat,$lng")
            append("&radius=$radius")
            append("&type=$type")
            append("&key=$apiKey")
            if (pageToken != null) append("&pagetoken=$pageToken")
        }

        return executeRequest(url)
    }

    /**
     * Fetches enriched details for a single place_id.
     */
    fun getPlaceDetails(placeId: String): PlaceDetails? {
        val fields = "place_id,name,formatted_address,formatted_phone_number,website,price_level,types,address_components,rating,user_ratings_total,geometry"
        val url = "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId&fields=$fields&key=$apiKey"

        val response: PlaceDetailsResponse? = executeRequest(url)
        return response?.result
    }

    private inline fun <reified T> executeRequest(url: String): T? {
        val request = Request.Builder().url(url).build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    System.err.println("HTTP Error: ${response.code} for URL: $url")
                    return null
                }
                val bodyString = response.body?.string() ?: return null
                json.decodeFromString<T>(bodyString)
            }
        } catch (e: Exception) {
            System.err.println("Request failed: ${e.message}")
            null
        }
    }
}