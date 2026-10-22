package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Household
import com.kromanid.splitzee.models.HouseholdError
import com.kromanid.splitzee.repositories.HouseholdRepo

sealed interface CreateHouseholdResult {
    data class Success(val household: Household) : CreateHouseholdResult
    data class Invalid(val error: HouseholdError) : CreateHouseholdResult
}

interface CreateHouseholdUC {
    /** Creates a household with the trimmed [name] and a "Me" member, and selects it. */
    suspend fun execute(name: String): CreateHouseholdResult
}

internal class CreateHouseholdUCImpl(private val householdRepo: HouseholdRepo) : CreateHouseholdUC {
    override suspend fun execute(name: String): CreateHouseholdResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return CreateHouseholdResult.Invalid(HouseholdError.BlankName)
        return CreateHouseholdResult.Success(householdRepo.createHousehold(trimmed))
    }
}
