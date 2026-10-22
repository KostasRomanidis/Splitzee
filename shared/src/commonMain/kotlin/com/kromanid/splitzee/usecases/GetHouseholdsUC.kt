package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Household
import com.kromanid.splitzee.repositories.HouseholdRepo
import kotlinx.coroutines.flow.Flow

interface GetHouseholdsUC {
    /** All households, in the order they were created. */
    fun execute(): Flow<List<Household>>
}

internal class GetHouseholdsUCImpl(private val householdRepo: HouseholdRepo) : GetHouseholdsUC {
    override fun execute(): Flow<List<Household>> = householdRepo.observeHouseholds()
}
