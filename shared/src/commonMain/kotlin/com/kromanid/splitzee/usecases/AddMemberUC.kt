package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Member
import com.kromanid.splitzee.models.MemberError
import com.kromanid.splitzee.repositories.HouseholdRepo

sealed interface AddMemberResult {
    data class Success(val member: Member) : AddMemberResult
    data class Invalid(val error: MemberError) : AddMemberResult
}

interface AddMemberUC {
    /** Adds a member with the trimmed [name] to the current household. */
    suspend fun execute(name: String): AddMemberResult
}

internal class AddMemberUCImpl(private val householdRepo: HouseholdRepo) : AddMemberUC {
    override suspend fun execute(name: String): AddMemberResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return AddMemberResult.Invalid(MemberError.BlankName)
        return AddMemberResult.Success(householdRepo.addMember(householdRepo.current().id, trimmed))
    }
}
