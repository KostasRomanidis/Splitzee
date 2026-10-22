package com.kromanid.splitzee.models

sealed interface ExpenseError {
    data object BlankDescription : ExpenseError
    data object NonPositiveAmount : ExpenseError
    data object BlankCategory : ExpenseError
    data class UnknownMember(val memberId: MemberId) : ExpenseError
    data object NoParticipants : ExpenseError
    data class DuplicateParticipant(val memberId: MemberId) : ExpenseError
    data class NonPositiveShare(val memberId: MemberId) : ExpenseError

    /** [difference] = expense amount minus the sum of the exact shares. */
    data class SumMismatch(val difference: Money) : ExpenseError

    /** [differenceBp] = 10000 minus the sum of the percentages, in basis points. */
    data class PercentSumMismatch(val differenceBp: Int) : ExpenseError
}

sealed interface MemberError {
    data object BlankName : MemberError
}

sealed interface HouseholdError {
    data object BlankName : HouseholdError
}
