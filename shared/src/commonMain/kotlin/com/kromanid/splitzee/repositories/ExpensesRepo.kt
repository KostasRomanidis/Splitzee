package com.kromanid.splitzee.repositories

interface ExpensesRepo {
    fun addExpense(amount: Long)
    fun getExpenses(): List<Long>
}

internal class ExpensesRepoImpl(): ExpensesRepo {
    override fun addExpense(amount: Long) {
        TODO("Not yet implemented")
    }

    override fun getExpenses(): List<Long> {
        TODO("Not yet implemented")
    }

}