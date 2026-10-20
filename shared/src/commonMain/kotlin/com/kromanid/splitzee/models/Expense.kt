package com.kromanid.splitzee.models

import kotlinx.datetime.LocalDate

data class Expense(
    val id: String,
    val description: String,
    val amount: Money,
    val category: Category,
    val date: LocalDate,
    val paidBy: MemberId,
    val split: SplitRule,
    val shares: List<ExpenseShare>,
)

data class ExpenseShare(val memberId: MemberId, val amount: Money)
