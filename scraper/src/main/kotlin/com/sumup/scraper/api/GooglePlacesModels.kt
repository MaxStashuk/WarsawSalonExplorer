package com.sumup.scraper.api

import kotlinx.serialization.Serializable

@Serializable
data class NearbySearchResponse(
    val results: List<PlaceSummary>,
    val next_page_token: String? = null
)

@Serializable
data class PlaceSummary(
    val place_id: String,
    val name: String,
    val types: List<String> = emptyList(),
    val rating: Double? = null,
    val user_ratings_total: Int? = null,
    val geometry: Geometry? = null
)

@Serializable
data class Geometry(
    val location: Location
)

@Serializable
data class Location(
    val lat: Double,
    val lng: Double
)

@Serializable
data class PlaceDetailsResponse(
    val result: PlaceDetails? = null
)

@Serializable
data class PlaceDetails(
    val place_id: String,
    val name: String,
    val formatted_address: String? = null,
    val formatted_phone_number: String? = null,
    val website: String? = null,
    val price_level: Int? = null,
    val types: List<String> = emptyList(),
    val address_components: List<AddressComponent> = emptyList(),
    val rating: Double? = null,
    val user_ratings_total: Int? = null,
    val geometry: Geometry? = null
)

@Serializable
data class AddressComponent(
    val long_name: String,
    val short_name: String,
    val types: List<String>
)