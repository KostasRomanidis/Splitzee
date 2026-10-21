package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.repositories.ExpensesRepo
import kotlinx.coroutines.flow.Flow

interface GetExpensesUC {
    fun getExpenses(): Flow<Map<String, Long>>
}

internal class GetExpensesUCImpl(private val repo: ExpensesRepo) : GetExpensesUC {
    override fun getExpenses(): Flow<Map<String, Long>> {
        TODO("replaced in task 4.1")
    }
}