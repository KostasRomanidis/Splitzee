package com.kromanid.splitzee.sources

import kotlinx.coroutines.flow.Flow

expect interface ExpensesSource {
    suspend fun save(key: String, value: Long)
    fun get(): Flow<Map<String, Long>>
}