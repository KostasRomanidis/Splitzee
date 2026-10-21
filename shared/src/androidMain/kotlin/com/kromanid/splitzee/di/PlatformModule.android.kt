package com.kromanid.splitzee.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.kromanid.splitzee.database.SplitzeeDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

// The Context comes from androidContext() in the app's initKoin config.
actual val platformModule: Module = module {
    single<SqlDriver> { AndroidSqliteDriver(SplitzeeDatabase.Schema, get(), "splitzee.db") }
    single { SplitzeeDatabase(get()) }
}
