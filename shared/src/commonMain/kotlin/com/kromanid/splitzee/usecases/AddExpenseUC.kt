package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.repositories.ExpensesRepo

interface AddExpenseUC {
    suspend fun execute(description: String, expense: Long)
}

internal class AddExpenseUCImpl(private val repo: ExpensesRepo) : AddExpenseUC {
    override suspend fun execute(description: String, expense: Long) {
        println("Expense added: $description : $expense")
        repo.addExpense(description = description, amount = expense)
    }
}