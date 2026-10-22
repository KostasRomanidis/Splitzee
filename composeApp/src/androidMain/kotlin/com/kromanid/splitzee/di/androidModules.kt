package com.kromanid.splitzee.di

import com.kromanid.splitzee.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val androidModules = module {
    viewModel {
        MainViewModel(
            addExpenseUC = get(),
            getExpensesUC = get(),
            getMembersUC = get(),
            createHouseholdUC = get(),
            getHouseholdsUC = get(),
            getCurrentHouseholdUC = get(),
            selectHouseholdUC = get(),
        )
    }
}