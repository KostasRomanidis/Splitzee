package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Category
import com.kromanid.splitzee.models.ExpenseError
import com.kromanid.splitzee.models.ExpenseShare
import com.kromanid.splitzee.models.FixedCategory
import com.kromanid.splitzee.models.HouseholdError
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
import kotlin.test.assertNull
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
        assertEquals(2, householdRepo.members.value.getValue("home").size)
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

    @Test
    fun observersFollowHouseholdSwitch() = runBlocking {
        addGroceries()
        val names = Channel<List<String>>(Channel.UNLIMITED)
        val expenseCounts = Channel<Int>(Channel.UNLIMITED)
        val observers = listOf(
            launch { GetMembersUCImpl(householdRepo).execute().collect { names.send(it.map { m -> m.name }) } },
            launch { GetExpensesUCImpl(householdRepo, expensesRepo).execute().collect { expenseCounts.send(it.size) } },
        )
        assertEquals(listOf("Ana", "Ben"), names.receive())
        assertEquals(1, expenseCounts.receive())

        householdRepo.createHousehold("Beach house")
        assertEquals(listOf("Me"), names.receive())
        assertEquals(0, expenseCounts.receive())

        householdRepo.selectHousehold("home")
        assertEquals(listOf("Ana", "Ben"), names.receive())
        assertEquals(1, expenseCounts.receive())
        observers.forEach { it.cancel() }
    }

    @Test
    fun noHouseholdGivesEmptyData() = runBlocking {
        addGroceries()
        householdRepo.selected.value = null

        assertEquals(emptyList(), GetMembersUCImpl(householdRepo).execute().first())
        assertEquals(emptyList(), GetExpensesUCImpl(householdRepo, expensesRepo).execute().first())
        val summary = GetSummaryUCImpl(householdRepo, expensesRepo).execute(Period.AllTime).first()
        assertEquals(Money(0), summary.total)
        assertEquals(emptyList(), summary.members)
        assertNull(GetCurrentHouseholdUCImpl(householdRepo).execute().first())
    }

    @Test
    fun addWithoutHouseholdStoresNothing() = runBlocking {
        householdRepo.selected.value = null

        assertEquals(AddMemberResult.NoHousehold, AddMemberUCImpl(householdRepo).execute("Cleo"))
        assertEquals(AddExpenseResult.NoHousehold, addGroceries())
        assertEquals(RemoveMemberResult.NoHousehold, RemoveMemberUCImpl(householdRepo).execute("ana"))
        assertEquals(2, householdRepo.members.value.getValue("home").size)
        assertEquals(emptyList(), expensesRepo.expenses.value)
    }

    @Test
    fun createHouseholdTrimsNameAndSelectsIt() = runBlocking {
        val result = assertIs<CreateHouseholdResult.Success>(CreateHouseholdUCImpl(householdRepo).execute("  Beach house "))

        assertEquals("Beach house", result.household.name)
        assertEquals("EUR", result.household.currency)
        assertEquals(result.household, GetCurrentHouseholdUCImpl(householdRepo).execute().first())
        assertEquals(listOf("Home", "Beach house"), GetHouseholdsUCImpl(householdRepo).execute().first().map { it.name })
    }

    @Test
    fun blankHouseholdNameStoresNothing() = runBlocking {
        assertEquals(CreateHouseholdResult.Invalid(HouseholdError.BlankName), CreateHouseholdUCImpl(householdRepo).execute("  "))
        assertEquals(listOf(home), householdRepo.households.value)
        assertEquals(home, householdRepo.selected.value)
    }

    @Test
    fun removedMemberLeavesListButStaysInSummary() = runBlocking {
        addGroceries()

        assertEquals(RemoveMemberResult.Removed, RemoveMemberUCImpl(householdRepo).execute("ana"))

        assertEquals(listOf("Ben"), GetMembersUCImpl(householdRepo).execute().first().map { it.name })
        val summary = GetSummaryUCImpl(householdRepo, expensesRepo).execute(Period.AllTime).first()
        assertEquals(listOf("Ben" to Money(-2250), "Ana" to Money(2250)), summary.members.map { it.member.name to it.balance })
        assertEquals(AddExpenseResult.Invalid(listOf(ExpenseError.UnknownMember("ana"))), addGroceries())
    }

    @Test
    fun ownerCannotBeRemoved() = runBlocking {
        val flat = assertIs<CreateHouseholdResult.Success>(CreateHouseholdUCImpl(householdRepo).execute("Flat")).household
        val me = GetMembersUCImpl(householdRepo).execute().first().single()

        assertEquals(RemoveMemberResult.CannotRemoveOwner, RemoveMemberUCImpl(householdRepo).execute(me.id))
        assertEquals(listOf(me), householdRepo.members.value.getValue(flat.id))
    }

    @Test
    fun deleteSelectedHouseholdSelectsAnother() = runBlocking {
        val beach = assertIs<CreateHouseholdResult.Success>(CreateHouseholdUCImpl(householdRepo).execute("Beach")).household
        SelectHouseholdUCImpl(householdRepo).execute(home.id)

        DeleteHouseholdUCImpl(householdRepo).execute(home.id)

        assertEquals(beach, householdRepo.selected.value)
    }
}
