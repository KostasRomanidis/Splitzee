package com.kromanid.splitzee.rules

import com.kromanid.splitzee.models.Category
import com.kromanid.splitzee.models.ExpenseError
import com.kromanid.splitzee.models.ExpenseShare
import com.kromanid.splitzee.models.FixedCategory
import com.kromanid.splitzee.models.Money
import com.kromanid.splitzee.models.NewExpense
import com.kromanid.splitzee.models.SplitRule
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpenseValidationTest {
    private val members = setOf("ana", "ben", "cleo")

    private val groceries = NewExpense(
        description = "Groceries",
        amount = Money(4500),
        category = Category.Fixed(FixedCategory.Groceries),
        date = LocalDate(2026, 10, 19),
        paidBy = "ana",
        split = SplitRule.Equal(listOf("ana", "ben")),
    )

    private fun errorsOf(expense: NewExpense) = (validateExpense(expense, members) as Validated.Invalid).errors

    @Test
    fun validExpense() {
        assertEquals(
            Validated.Valid(ValidExpense(groceries, listOf(ExpenseShare("ana", Money(2250)), ExpenseShare("ben", Money(2250))))),
            validateExpense(groceries, members),
        )
    }

    @Test
    fun blankDescriptionAndZeroAmount() {
        assertEquals(
            listOf(ExpenseError.BlankDescription, ExpenseError.NonPositiveAmount),
            errorsOf(groceries.copy(description = "  ", amount = Money(0))),
        )
    }

    @Test
    fun negativeAmount() {
        assertEquals(listOf(ExpenseError.NonPositiveAmount), errorsOf(groceries.copy(amount = Money(-100))))
    }

    @Test
    fun unknownPayer() {
        assertEquals(listOf(ExpenseError.UnknownMember("zoe")), errorsOf(groceries.copy(paidBy = "zoe")))
    }

    @Test
    fun unknownParticipant() {
        assertEquals(
            listOf(ExpenseError.UnknownMember("zoe")),
            errorsOf(groceries.copy(split = SplitRule.Equal(listOf("ana", "zoe")))),
        )
    }

    @Test
    fun splitErrorsAreIncluded() {
        assertEquals(
            listOf(ExpenseError.BlankDescription, ExpenseError.SumMismatch(Money(500))),
            errorsOf(groceries.copy(description = "", split = SplitRule.Exact(listOf("ana" to Money(4000))))),
        )
    }

    @Test
    fun payerNeedNotParticipate() {
        val result = validateExpense(
            groceries.copy(amount = Money(2000), split = SplitRule.Equal(listOf("ben", "cleo"))),
            members,
        )
        assertEquals(
            listOf(ExpenseShare("ben", Money(1000)), ExpenseShare("cleo", Money(1000))),
            (result as Validated.Valid).value.shares,
        )
    }

    @Test
    fun customCategoryIsTrimmed() {
        val result = validateExpense(groceries.copy(category = Category.Custom(" Pet food ")), members)
        assertEquals(Category.Custom("Pet food"), (result as Validated.Valid).value.expense.category)
    }

    @Test
    fun blankCustomCategory() {
        assertEquals(listOf(ExpenseError.BlankCategory), errorsOf(groceries.copy(category = Category.Custom("   "))))
    }
}
