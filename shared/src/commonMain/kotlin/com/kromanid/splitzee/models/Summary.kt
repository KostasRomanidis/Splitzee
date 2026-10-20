package com.kromanid.splitzee.models

data class Summary(
    val total: Money,
    val byCategory: List<CategoryTotal>,
    val members: List<MemberSummary>,
)

data class CategoryTotal(val category: Category, val total: Money)

data class MemberSummary(val member: Member, val paid: Money, val share: Money) {
    /** Positive: the member is owed money. Negative: the member owes. */
    val balance: Money get() = paid - share
}
