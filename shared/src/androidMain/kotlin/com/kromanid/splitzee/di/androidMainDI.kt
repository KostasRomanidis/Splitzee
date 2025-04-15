package com.kromanid.splitzee.di

import androidx.datastore.dataStore
import com.kromanid.splitzee.sources.ExpensesSource
import com.kromanid.splitzee.sources.ExpensesSourceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val androidMain = module {
    singleOf(::ExpensesSourceImpl).bind<ExpensesSource>()
}