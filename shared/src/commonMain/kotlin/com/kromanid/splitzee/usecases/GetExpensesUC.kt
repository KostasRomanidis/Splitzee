package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Expense
import com.kromanid.splitzee.repositories.ExpensesRepo
import com.kromanid.splitzee.repositories.HouseholdRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

interface GetExpensesUC {
    /** The selected household's expenses, newest first; follows a household switch. Empty with no household. */
    fun execute(): Flow<List<Expense>>
}

internal class GetExpensesUCImpl(
    private val householdRepo: HouseholdRepo,
    private val expensesRepo: ExpensesRepo,
) : GetExpensesUC {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun execute(): Flow<List<Expense>> = householdRepo.observeCurrent().flatMapLatest { household ->
        household?.let { expensesRepo.observeExpenses(it.id) } ?: flowOf(emptyList())
    }
}
