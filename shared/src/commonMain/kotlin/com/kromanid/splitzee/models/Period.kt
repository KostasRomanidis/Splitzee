package com.kromanid.splitzee.models

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

sealed interface Period {
    /** `[start, end)` dates of the period, or null for all time. */
    val range: OpenEndRange<LocalDate>?

    data object AllTime : Period {
        override val range: OpenEndRange<LocalDate>? = null
    }

    data class Month(val year: Int, val month: Int) : Period {
        override val range: OpenEndRange<LocalDate>
            get() = LocalDate(year, month, 1).let { it..<it.plus(1, DateTimeUnit.MONTH) }
    }

    data class Year(val year: Int) : Period {
        override val range: OpenEndRange<LocalDate>
            get() = LocalDate(year, 1, 1)..<LocalDate(year + 1, 1, 1)
    }
}
