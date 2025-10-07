package com.kromanid.splitzee.sources

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSNumber
import platform.Foundation.NSUserDefaults

private const val USER_EXPENSES_KEY = "user_expenses"

actual interface ExpensesSource {
    actual suspend fun save(key: String, value: Long)
    actual fun get(): Flow<Map<String, Long>>
}