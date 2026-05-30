package com.sumup.backend.db

import com.sumup.backend.models.SalonDetail
import com.sumup.backend.models.SalonPatchRequest
import com.sumup.backend.models.SalonSummary
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object SalonRepository {

    fun getSalons(
        district: String?,
        q: String?,
        sort: String?,
        page: Int,
        pageSize: Int,
    ): Pair<List<SalonSummary>, Int> = transaction {
        fun base() = Salons.selectAll().apply {
            if (!district.isNullOrBlank()) andWhere { Salons.district eq district }
            if (!q.isNullOrBlank()) andWhere { Salons.name like "%$q%" }
        }

        val total = base().count().toInt()

        val items = base().apply {
            when (sort) {
                "name"    -> orderBy(Salons.name to SortOrder.ASC)
                "reviews" -> orderBy(Salons.reviewsCount to SortOrder.DESC_NULLS_LAST)
                else      -> orderBy(Salons.rating to SortOrder.DESC_NULLS_LAST)
            }
            limit(pageSize, offset = ((page - 1).toLong() * pageSize))
        }.map { it.toSalonSummary() }

        Pair(items, total)
    }

    fun getSalonById(id: Int): SalonDetail? = transaction {
        Salons.selectAll().where { Salons.id eq id }.singleOrNull()?.toSalonDetail()
    }

    fun patchSalon(id: Int, patch: SalonPatchRequest): SalonDetail? = transaction {
        val updated = Salons.update({ Salons.id eq id }) { stmt ->
            patch.name?.let     { stmt[Salons.name] = it }
            patch.address?.let  { stmt[Salons.address] = it }
            patch.district?.let { stmt[Salons.district] = it }
            patch.services?.let { stmt[Salons.services] = Json.encodeToString(it) }
            patch.priceLevel?.let { stmt[Salons.priceLevel] = it }
            stmt[Salons.updatedAt] = System.currentTimeMillis()
        }
        if (updated == 0) return@transaction null
        Salons.selectAll().where { Salons.id eq id }.single().toSalonDetail()
    }

    fun getDistricts(): List<String> = transaction {
        Salons.selectAll()
            .map { it[Salons.district] }
            .distinct()
            .sorted()
    }

    private fun ResultRow.toSalonSummary() = SalonSummary(
        id           = this[Salons.id].value,
        name         = this[Salons.name],
        district     = this[Salons.district],
        rating       = this[Salons.rating],
        reviewsCount = this[Salons.reviewsCount],
        priceLevel   = this[Salons.priceLevel]
    )

    private fun ResultRow.toSalonDetail() = SalonDetail(
        id           = this[Salons.id].value,
        placeId      = this[Salons.placeId],
        name         = this[Salons.name],
        address      = this[Salons.address],
        district     = this[Salons.district],
        phone        = this[Salons.phone],
        website      = this[Salons.website],
        services     = this[Salons.services]?.let { Json.decodeFromString<List<String>>(it) },
        priceLevel   = this[Salons.priceLevel],
        rating       = this[Salons.rating],
        reviewsCount = this[Salons.reviewsCount],
        lat          = this[Salons.lat],
        lng          = this[Salons.lng],
        updatedAt    = this[Salons.updatedAt]
    )
}
