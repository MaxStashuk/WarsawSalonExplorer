package com.sumup.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class SalonSummary(
    val id: Int,
    val name: String,
    val district: String,
    val rating: Double?,
    val reviewsCount: Int?,
    val priceLevel: Int?
)

@Serializable
data class SalonDetail(
    val id: Int,
    val placeId: String,
    val name: String,
    val address: String,
    val district: String,
    val phone: String?,
    val website: String?,
    val services: List<String>?,
    val priceLevel: Int?,
    val rating: Double?,
    val reviewsCount: Int?,
    val lat: Double?,
    val lng: Double?,
    val updatedAt: Long
)

@Serializable
data class SalonListResponse(
    val items: List<SalonSummary>,
    val total: Int
)

@Serializable
data class ErrorResponse(val error: String, val code: String)

@Serializable
data class SalonPatchRequest(
    val name: String? = null,
    val address: String? = null,
    val district: String? = null,
    val services: List<String>? = null,
    val priceLevel: Int? = null
)
