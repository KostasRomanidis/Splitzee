package com.kromanid.splitzee.rules

import com.kromanid.splitzee.models.Category
import com.kromanid.splitzee.models.ExpenseError
import com.kromanid.splitzee.models.ExpenseShare
import com.kromanid.splitzee.models.MemberId
import com.kromanid.splitzee.models.NewExpense

internal data class ValidExpense(val expense: NewExpense, val shares: List<ExpenseShare>)

/** Checks [expense] against the household's [members], collecting every error. A valid result has a trimmed custom category. */
internal fun validateExpense(expense: NewExpense, members: Set<MemberId>): Validated<ValidExpense> {
    val category = (expense.category as? Category.Custom)?.let { Category.Custom(it.name.trim()) } ?: expense.category
    val errors = mutableListOf<ExpenseError>()
    if (expense.description.isBlank()) errors += ExpenseError.BlankDescription
    if (expense.amount.minor <= 0) errors += ExpenseError.NonPositiveAmount
    if (category is Category.Custom && category.name.isEmpty()) errors += ExpenseError.BlankCategory
    (listOf(expense.paidBy) + expense.split.participants)
        .filter { it !in members }
        .distinct()
        .mapTo(errors) { ExpenseError.UnknownMember(it) }

    return when (val shares = computeShares(expense.amount, expense.paidBy, expense.split)) {
        is Validated.Invalid -> Validated.Invalid(errors + shares.errors)
        is Validated.Valid ->
            if (errors.isEmpty()) Validated.Valid(ValidExpense(expense.copy(category = category), shares.value))
            else Validated.Invalid(errors)
    }
}
