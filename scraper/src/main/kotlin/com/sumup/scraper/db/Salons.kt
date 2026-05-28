package com.sumup.scraper.db

import org.jetbrains.exposed.dao.id.IntIdTable

object Salons : IntIdTable("salons") {
    val placeId = varchar("place_id", 128).uniqueIndex()
    val name = varchar("name", 256)
    val address = varchar("address", 512)
    val district = varchar("district", 64).index()
    val phone = varchar("phone", 64).nullable()
    val website = varchar("website", 512).nullable()
    val services = text("services").nullable() // Will store JSON array as String
    val priceLevel = integer("price_level").nullable()
    val rating = double("rating").nullable()
    val reviewsCount = integer("reviews_count").nullable()
    val lat = double("lat").nullable()
    val lng = double("lng").nullable()
    val updatedAt = long("updated_at")
}