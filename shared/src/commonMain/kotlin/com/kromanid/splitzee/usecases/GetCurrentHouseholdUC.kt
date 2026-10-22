package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Household
import com.kromanid.splitzee.repositories.HouseholdRepo
import kotlinx.coroutines.flow.Flow

interface GetCurrentHouseholdUC {
    /** The selected household, or null when none exists. */
    fun execute(): Flow<Household?>
}

internal class GetCurrentHouseholdUCImpl(private val householdRepo: HouseholdRepo) : GetCurrentHouseholdUC {
    override fun execute(): Flow<Household?> = householdRepo.observeCurrent()
}
