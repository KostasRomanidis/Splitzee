package com.kromanid.splitzee.repositories

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kromanid.splitzee.database.SplitzeeDatabase
import kotlin.time.Clock
import kotlin.time.Instant

internal fun inMemoryDriver() = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { SplitzeeDatabase.Schema.create(it) }

/** Advances one millisecond per call, so creation order is deterministic. */
internal class TickingClock(private var millis: Long = 1_000) : Clock {
    override fun now() = Instant.fromEpochMilliseconds(millis++)
}

internal fun JdbcSqliteDriver.count(table: String): Long =
    executeQuery(null, "SELECT count(*) FROM $table", { it.next(); QueryResult.Value(it.getLong(0)!!) }, 0).value
