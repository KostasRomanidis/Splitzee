package com.kromanid.splitzee.models

import kotlin.jvm.JvmInline

/** An amount in the household currency's minor unit (e.g. cents). */
@JvmInline
value class Money(val minor: Long) {
    operator fun plus(other: Money) = Money(minor + other.minor)
    operator fun minus(other: Money) = Money(minor - other.minor)

    companion object {
        val ZERO = Money(0)
    }
}
