package com.kromanid.splitzee.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.kromanid.splitzee.database.SplitzeeDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SqlDriver> { NativeSqliteDriver(SplitzeeDatabase.Schema, "splitzee.db") }
    single { SplitzeeDatabase(get()) }
}
