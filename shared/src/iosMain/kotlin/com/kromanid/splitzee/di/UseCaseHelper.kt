package com.kromanid.splitzee.di

import com.kromanid.splitzee.usecases.AddExpenseUC
import com.kromanid.splitzee.usecases.GetExpensesUC
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class UseCaseHelper : KoinComponent {
    fun getAddExpenseUC(): AddExpenseUC = get()
    fun getExpensesUC(): GetExpensesUC = get()
}