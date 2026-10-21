package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Expense
import com.kromanid.splitzee.repositories.ExpensesRepo
import com.kromanid.splitzee.repositories.HouseholdRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

interface GetExpensesUC {
    /** The current household's expenses, newest first. */
    fun execute(): Flow<List<Expense>>
}

internal class GetExpensesUCImpl(
    private val householdRepo: HouseholdRepo,
    private val expensesRepo: ExpensesRepo,
) : GetExpensesUC {
    override fun execute(): Flow<List<Expense>> = flow {
        emitAll(expensesRepo.observeExpenses(householdRepo.current().id))
    }
}
