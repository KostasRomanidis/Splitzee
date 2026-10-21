package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Member
import com.kromanid.splitzee.repositories.HouseholdRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

interface GetMembersUC {
    /** The current household's members, in the order they were added. */
    fun execute(): Flow<List<Member>>
}

internal class GetMembersUCImpl(private val householdRepo: HouseholdRepo) : GetMembersUC {
    override fun execute(): Flow<List<Member>> = flow {
        emitAll(householdRepo.observeMembers(householdRepo.current().id))
    }
}
