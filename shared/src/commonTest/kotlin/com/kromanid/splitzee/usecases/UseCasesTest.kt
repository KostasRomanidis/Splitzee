package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Category
import com.kromanid.splitzee.models.ExpenseError
import com.kromanid.splitzee.models.ExpenseShare
import com.kromanid.splitzee.models.FixedCategory
import com.kromanid.splitzee.models.Member
import com.kromanid.splitzee.models.MemberError
import com.kromanid.splitzee.models.Money
import com.kromanid.splitzee.models.Period
import com.kromanid.splitzee.models.SplitRule
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class UseCasesTest {
    private val householdRepo = FakeHouseholdRepo(Member("ana", "Ana"), Member("ben", "Ben"))
    private val expensesRepo = FakeExpensesRepo()
    // Noon UTC stays on 2026-10-19 in any time zone within ±12h.
    private val clock = FixedClock(Instant.parse("2026-10-19T12:00:00Z"))
    private val addExpense = AddExpenseUCImpl(householdRepo, expensesRepo, clock)

    private suspend fun addGroceries(date: LocalDate? = LocalDate(2026, 10, 1), amount: Money = Money(4500)) =
        addExpense.execute(
            "Groceries", amount, Category.Fixed(FixedCategory.Groceries), "ana", SplitRule.Equal(listOf("ana", "ben")), date,
        )

    @Test
    fun addExpenseStoresSharesFromSplit() = runBlocking {
        val result = assertIs<AddExpenseResult.Success>(addGroceries())

        assertEquals(listOf(ExpenseShare("ana", Money(2250)), ExpenseShare("ben", Money(2250))), result.expense.shares)
        assertEquals(listOf(result.expense), expensesRepo.expenses.value)
    }

    @Test
    fun addExpenseDefaultsDateToToday() = runBlocking {
        val result = assertIs<AddExpenseResult.Success>(addGroceries(date = null))

        assertEquals(LocalDate(2026, 10, 19), result.expense.date)
    }

    @Test
    fun addExpenseStoresTrimmedCustomCategory() = runBlocking {
        val result = addExpense.execute("Kibble", Money(1000), Category.Custom(" Pet food "), "ana", SplitRule.Equal(listOf("ana")))

        assertEquals(Category.Custom("Pet food"), assertIs<AddExpenseResult.Success>(result).expense.category)
    }

    @Test
    fun invalidExpenseStoresNothing() = runBlocking {
        val result = addExpense.execute("", Money(0), Category.Fixed(FixedCategory.Other), "zed", SplitRule.Equal(listOf("ana")))

        assertEquals(
            AddExpenseResult.Invalid(
                listOf(ExpenseError.BlankDescription, ExpenseError.NonPositiveAmount, ExpenseError.UnknownMember("zed"))
            ),
            result,
        )
        assertEquals(emptyList(), expensesRepo.expenses.value)
    }

    @Test
    fun getExpensesObservesCurrentHousehold() = runBlocking {
        val added = assertIs<AddExpenseResult.Success>(addGroceries()).expense

        assertEquals(listOf(added), GetExpensesUCImpl(householdRepo, expensesRepo).execute().first())
    }

    @Test
    fun addMemberTrimsName() = runBlocking {
        val result = assertIs<AddMemberResult.Success>(AddMemberUCImpl(householdRepo).execute("  Cleo "))

        assertEquals("Cleo", result.member.name)
        assertEquals(listOf("Ana", "Ben", "Cleo"), GetMembersUCImpl(householdRepo).execute().first().map { it.name })
    }

    @Test
    fun blankMemberNameStoresNothing() = runBlocking {
        assertEquals(AddMemberResult.Invalid(MemberError.BlankName), AddMemberUCImpl(householdRepo).execute("   "))
        assertEquals(2, householdRepo.members.value.size)
    }

    @Test
    fun summaryCoversOnlyThePeriod() = runBlocking {
        addGroceries(date = LocalDate(2026, 9, 30), amount = Money(1000))
        addGroceries(date = LocalDate(2026, 10, 1))

        val summary = GetSummaryUCImpl(householdRepo, expensesRepo).execute(Period.Month(2026, 10)).first()

        assertEquals(Money(4500), summary.total)
        assertEquals(listOf(Money(2250), Money(-2250)), summary.members.map { it.balance })
    }

    @Test
    fun summaryUpdatesWhenExpenseAdded() = runBlocking {
        val totals = Channel<Money>(Channel.UNLIMITED)
        val observer = launch { GetSummaryUCImpl(householdRepo, expensesRepo).execute(Period.AllTime).collect { totals.send(it.total) } }

        assertEquals(Money(0), totals.receive())
        addGroceries()
        assertEquals(Money(4500), totals.receive())
        observer.cancel()
    }
}
