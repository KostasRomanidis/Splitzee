package com.kromanid.splitzee.repositories

import com.kromanid.splitzee.sources.ExpensesSource
import kotlinx.coroutines.flow.Flow

interface ExpensesRepo {
    suspend fun addExpense(description: String, amount: Long)
    fun getExpenses(): Flow<Map<String, Long>>
}

internal class ExpensesRepoImpl(private val source: ExpensesSource) : ExpensesRepo {
    override suspend fun addExpense(description: String, amount: Long) {
        source.save(key = description, value = amount)
    }

    override fun getExpenses(): Flow<Map<String, Long>> {
        return source.get()
    }
}