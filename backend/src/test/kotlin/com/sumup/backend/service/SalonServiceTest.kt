package com.sumup.backend.service

import com.sumup.backend.models.SalonPatchRequest
import kotlin.test.Test
import kotlin.test.assertIs

class SalonServiceTest {

    @Test
    fun `patchSalon blank name returns ValidationError`() {
        assertIs<PatchResult.ValidationError>(SalonService.patchSalon(1, SalonPatchRequest(name = "  ")))
    }

    @Test
    fun `patchSalon blank address returns ValidationError`() {
        assertIs<PatchResult.ValidationError>(SalonService.patchSalon(1, SalonPatchRequest(address = "")))
    }

    @Test
    fun `patchSalon blank district returns ValidationError`() {
        assertIs<PatchResult.ValidationError>(SalonService.patchSalon(1, SalonPatchRequest(district = " ")))
    }

    @Test
    fun `patchSalon priceLevel below range returns ValidationError`() {
        assertIs<PatchResult.ValidationError>(SalonService.patchSalon(1, SalonPatchRequest(priceLevel = -1)))
    }

    @Test
    fun `patchSalon priceLevel above range returns ValidationError`() {
        assertIs<PatchResult.ValidationError>(SalonService.patchSalon(1, SalonPatchRequest(priceLevel = 5)))
    }
}
