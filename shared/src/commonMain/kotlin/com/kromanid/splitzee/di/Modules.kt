package com.kromanid.splitzee.di

import com.kromanid.splitzee.repositories.ExpensesRepo
import com.kromanid.splitzee.repositories.ExpensesRepoImpl
import com.kromanid.splitzee.usecases.AddExpenseUC
import com.kromanid.splitzee.usecases.AddExpenseUCImpl
import com.kromanid.splitzee.usecases.GetExpensesUC
import com.kromanid.splitzee.usecases.GetExpensesUCImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val commonModules = module {
    // usecases
    factoryOf(::AddExpenseUCImpl).bind<AddExpenseUC>()
    factoryOf(::GetExpensesUCImpl).bind<GetExpensesUC>()

    // repositories
    factoryOf(::ExpensesRepoImpl).bind<ExpensesRepo>()

}