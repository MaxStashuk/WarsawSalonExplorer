package com.sumup.backend.service

import com.sumup.backend.db.SalonRepository
import com.sumup.backend.models.SalonDetail
import com.sumup.backend.models.SalonListResponse
import com.sumup.backend.models.SalonPatchRequest

sealed class PatchResult {
    data class Success(val salon: SalonDetail) : PatchResult()
    data object NotFound : PatchResult()
    data class ValidationError(val message: String) : PatchResult()
}

object SalonService {
    fun getSalons(
        district: String?,
        q: String?,
        sort: String?,
        page: Int = 1,
        pageSize: Int = 20,
    ): SalonListResponse {
        val (items, total) = SalonRepository.getSalons(district, q, sort, page, pageSize)
        val totalPages = if (total == 0) 1 else (total + pageSize - 1) / pageSize
        return SalonListResponse(
            items      = items,
            total      = total,
            page       = page,
            pageSize   = pageSize,
            totalPages = totalPages,
        )
    }

    fun getSalonById(id: Int): SalonDetail? = SalonRepository.getSalonById(id)

    fun patchSalon(id: Int, patch: SalonPatchRequest): PatchResult {
        if (patch.name != null && patch.name.isBlank())
            return PatchResult.ValidationError("name must not be blank")
        if (patch.address != null && patch.address.isBlank())
            return PatchResult.ValidationError("address must not be blank")
        if (patch.district != null && patch.district.isBlank())
            return PatchResult.ValidationError("district must not be blank")
        if (patch.priceLevel != null && patch.priceLevel !in 0..4)
            return PatchResult.ValidationError("priceLevel must be between 0 and 4")

        val updated = SalonRepository.patchSalon(id, patch)
            ?: return PatchResult.NotFound
        return PatchResult.Success(updated)
    }

    fun getDistricts(): List<String> = SalonRepository.getDistricts()
}
