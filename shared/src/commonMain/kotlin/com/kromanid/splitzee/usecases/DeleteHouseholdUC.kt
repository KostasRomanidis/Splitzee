package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.repositories.HouseholdRepo

interface DeleteHouseholdUC {
    /** Deletes the household with [id]; the most recently selected remaining household becomes selected. */
    suspend fun execute(id: String)
}

internal class DeleteHouseholdUCImpl(private val householdRepo: HouseholdRepo) : DeleteHouseholdUC {
    override suspend fun execute(id: String) = householdRepo.deleteHousehold(id)
}
