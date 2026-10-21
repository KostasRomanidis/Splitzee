package com.kromanid.splitzee.repositories

import com.kromanid.splitzee.database.SplitzeeDatabase
import com.kromanid.splitzee.models.Category
import com.kromanid.splitzee.models.ExpenseShare
import com.kromanid.splitzee.models.FixedCategory
import com.kromanid.splitzee.models.Money
import com.kromanid.splitzee.models.NewExpense
import com.kromanid.splitzee.models.Period
import com.kromanid.splitzee.models.SplitRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ExpensesRepoTest {
    private val driver = inMemoryDriver()
    private val repo = ExpensesRepoImpl(SplitzeeDatabase(driver), TickingClock())

    private fun expense(description: String = "Groceries", date: String = "2026-10-01") = NewExpense(
        description = description,
        amount = Money(4500),
        category = Category.Fixed(FixedCategory.Groceries),
        date = LocalDate.parse(date),
        paidBy = "ana",
        split = SplitRule.Equal(listOf("ana", "ben")),
    )

    private val equalShares = listOf(ExpenseShare("ana", Money(2250)), ExpenseShare("ben", Money(2250)))

    private suspend fun add(e: NewExpense = expense(), shares: List<ExpenseShare> = equalShares) =
        repo.addExpense(HOUSEHOLD, e, shares)

    private suspend fun all() = repo.observeExpenses(HOUSEHOLD).first()

    @Test
    fun roundTripsAllFields() = runBlocking {
        val percent = NewExpense(
            description = "Cat stuff",
            amount = Money(1000),
            category = Category.Custom("Pet food"),
            date = LocalDate(2026, 10, 19),
            paidBy = "cleo",
            split = SplitRule.Percent(listOf("ana" to 3333, "ben" to 3333, "cleo" to 3334)),
        )
        val percentShares = listOf(ExpenseShare("ana", Money(333)), ExpenseShare("ben", Money(333)), ExpenseShare("cleo", Money(334)))
        val exact = expense("Rent").copy(split = SplitRule.Exact(listOf("ben" to Money(3000), "ana" to Money(1500))))
        val exactShares = listOf(ExpenseShare("ben", Money(3000)), ExpenseShare("ana", Money(1500)))

        val stored = listOf(add(percent, percentShares), add(exact, exactShares), add())

        assertEquals(stored.toSet(), all().toSet())
        assertEquals(percent.split, stored[0].split)
    }

    @Test
    fun sameDescriptionKeepsBoth() = runBlocking {
        add()
        add()
        assertEquals(2, all().size)
    }

    @Test
    fun ordersByDateThenCreatedDescending() = runBlocking {
        val early = add(expense("early", "2026-10-01"))
        val lateFirst = add(expense("late first", "2026-10-15"))
        val lateSecond = add(expense("late second", "2026-10-15"))

        assertEquals(listOf(lateSecond, lateFirst, early).map { it.id }, all().map { it.id })
    }

    @Test
    fun periodFiltersByDateRange() = runBlocking {
        add(expense("september", "2026-09-30"))
        val october = add(expense("october", "2026-10-01"))

        assertEquals(listOf(october.id), repo.observeExpenses(HOUSEHOLD, Period.Month(2026, 10)).first().map { it.id })
    }

    @Test
    fun deletedRowsExcluded() = runBlocking {
        val deleted = add()
        val kept = add(expense("kept"))
        driver.execute(null, "UPDATE expense SET deleted_at = 1 WHERE id = '${deleted.id}'", 0)

        assertEquals(listOf(kept.id), all().map { it.id })
        assertEquals(listOf(kept.id), repo.observeExpenses(HOUSEHOLD, Period.Year(2026)).first().map { it.id })
    }

    @Test
    fun failedShareWriteRollsBackExpense() = runBlocking {
        driver.execute(
            null,
            "CREATE TRIGGER fail_share BEFORE INSERT ON expense_share WHEN NEW.member_id = 'boom' " +
                "BEGIN SELECT RAISE(ABORT, 'boom'); END",
            0,
        )

        assertFails { add(shares = listOf(ExpenseShare("ana", Money(2250)), ExpenseShare("boom", Money(2250)))) }
        assertEquals(0L, driver.count("expense"))
        assertEquals(0L, driver.count("expense_share"))
    }

    private companion object {
        const val HOUSEHOLD = "h1"
    }
}
