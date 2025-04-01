package com.kromanid.splitzee.usecases

interface AddExpenseUC {
    fun execute(expense: Long)
}

class AddExpenseUCImpl() : AddExpenseUC {
    override fun execute(expense: Long) {
        println("Expense added: $expense")
    }
}