package com.kromanid.splitzee

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn

fun daysUntilNewYear(): Int {
    val today = Clock.System.todayIn(timeZone = TimeZone.currentSystemDefault())
    val closestNewYear = LocalDate(year = today.year + 1, monthNumber = 1, dayOfMonth = 1)
    return today.daysUntil(closestNewYear)
}

fun daysPhase(): String = "There are only ${daysUntilNewYear()} days left until new year!"