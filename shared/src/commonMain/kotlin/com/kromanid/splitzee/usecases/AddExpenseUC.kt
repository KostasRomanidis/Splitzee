package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Category
import com.kromanid.splitzee.models.Expense
import com.kromanid.splitzee.models.ExpenseError
import com.kromanid.splitzee.models.MemberId
import com.kromanid.splitzee.models.Money
import com.kromanid.splitzee.models.NewExpense
import com.kromanid.splitzee.models.SplitRule
import com.kromanid.splitzee.repositories.ExpensesRepo
import com.kromanid.splitzee.repositories.HouseholdRepo
import com.kromanid.splitzee.rules.Validated
import com.kromanid.splitzee.rules.validateExpense
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

sealed interface AddExpenseResult {
    data class Success(val expense: Expense) : AddExpenseResult
    data class Invalid(val errors: List<ExpenseError>) : AddExpenseResult
}

interface AddExpenseUC {
    /** Adds an expense to the current household. [date] defaults to today. */
    suspend fun execute(
        description: String,
        amount: Money,
        category: Category,
        paidBy: MemberId,
        split: SplitRule,
        date: LocalDate? = null,
    ): AddExpenseResult
}

internal class AddExpenseUCImpl(
    private val householdRepo: HouseholdRepo,
    private val expensesRepo: ExpensesRepo,
    private val clock: Clock,
) : AddExpenseUC {
    override suspend fun execute(
        description: String,
        amount: Money,
        category: Category,
        paidBy: MemberId,
        split: SplitRule,
        date: LocalDate?,
    ): AddExpenseResult {
        val household = householdRepo.current()
        val members = householdRepo.observeMembers(household.id).first().map { it.id }.toSet()
        val expense = NewExpense(
            description, amount, category, date ?: clock.todayIn(TimeZone.currentSystemDefault()), paidBy, split,
        )
        return when (val result = validateExpense(expense, members)) {
            is Validated.Invalid -> AddExpenseResult.Invalid(result.errors)
            is Validated.Valid -> AddExpenseResult.Success(
                expensesRepo.addExpense(household.id, result.value.expense, result.value.shares)
            )
        }
    }
}
