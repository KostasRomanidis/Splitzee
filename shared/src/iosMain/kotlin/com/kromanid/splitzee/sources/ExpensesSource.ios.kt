package com.kromanid.splitzee.sources

actual interface ExpensesSource {
    actual fun save(value: Long)
    actual fun get(): List<Long>
}