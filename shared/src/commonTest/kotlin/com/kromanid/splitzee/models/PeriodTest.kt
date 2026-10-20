package com.kromanid.splitzee.models

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PeriodTest {
    @Test
    fun monthBoundary() {
        val range = Period.Month(2026, 10).range
        assertFalse(LocalDate(2026, 9, 30) in range)
        assertTrue(LocalDate(2026, 10, 1) in range)
        assertTrue(LocalDate(2026, 10, 31) in range)
        assertFalse(LocalDate(2026, 11, 1) in range)
    }

    @Test
    fun decemberEndsAtNextYear() {
        assertEquals(LocalDate(2027, 1, 1), Period.Month(2026, 12).range.endExclusive)
    }

    @Test
    fun year() {
        val range = Period.Year(2026).range
        assertFalse(LocalDate(2025, 12, 31) in range)
        assertTrue(LocalDate(2026, 1, 1) in range)
        assertTrue(LocalDate(2026, 12, 31) in range)
        assertFalse(LocalDate(2027, 1, 1) in range)
    }

    @Test
    fun allTimeHasNoRange() {
        assertNull(Period.AllTime.range)
    }
}
