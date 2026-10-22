package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.repositories.HouseholdRepo

interface SelectHouseholdUC {
    /** Makes the household with [id] the selected one; unknown or deleted ids are ignored. */
    suspend fun execute(id: String)
}

internal class SelectHouseholdUCImpl(private val householdRepo: HouseholdRepo) : SelectHouseholdUC {
    override suspend fun execute(id: String) = householdRepo.selectHousehold(id)
}
