package com.kromanid.splitzee.rules

import com.kromanid.splitzee.models.ExpenseError

internal sealed interface Validated<out T> {
    data class Valid<T>(val value: T) : Validated<T>
    data class Invalid(val errors: List<ExpenseError>) : Validated<Nothing>
}
