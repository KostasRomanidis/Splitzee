package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.MemberId
import com.kromanid.splitzee.repositories.HouseholdRepo

sealed interface RemoveMemberResult {
    data object Removed : RemoveMemberResult
    data object CannotRemoveOwner : RemoveMemberResult
    data object NoHousehold : RemoveMemberResult
}

interface RemoveMemberUC {
    /** Removes [memberId] from the selected household; they stay on existing expenses and in summaries. */
    suspend fun execute(memberId: MemberId): RemoveMemberResult
}

internal class RemoveMemberUCImpl(private val householdRepo: HouseholdRepo) : RemoveMemberUC {
    override suspend fun execute(memberId: MemberId): RemoveMemberResult {
        val household = householdRepo.current() ?: return RemoveMemberResult.NoHousehold
        return if (householdRepo.removeMember(household.id, memberId)) RemoveMemberResult.Removed
        else RemoveMemberResult.CannotRemoveOwner
    }
}
