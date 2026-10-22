package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.repositories.HouseholdRepo

interface DeleteHouseholdUC {
    /**
     * Deletes the household with [id]; the most recently selected remaining household becomes selected.
     * Returns false, changing nothing, when the local user did not create it.
     */
    suspend fun execute(id: String): Boolean
}

internal class DeleteHouseholdUCImpl(private val householdRepo: HouseholdRepo) : DeleteHouseholdUC {
    override suspend fun execute(id: String) = householdRepo.deleteHousehold(id)
}
