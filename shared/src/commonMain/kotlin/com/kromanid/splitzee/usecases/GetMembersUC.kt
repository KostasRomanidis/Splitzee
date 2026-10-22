package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Member
import com.kromanid.splitzee.repositories.HouseholdRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

interface GetMembersUC {
    /** The selected household's members, in the order they were added; follows a household switch. Empty with no household. */
    fun execute(): Flow<List<Member>>
}

internal class GetMembersUCImpl(private val householdRepo: HouseholdRepo) : GetMembersUC {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun execute(): Flow<List<Member>> = householdRepo.observeCurrent().flatMapLatest { household ->
        household?.let { householdRepo.observeMembers(it.id) } ?: flowOf(emptyList())
    }
}
