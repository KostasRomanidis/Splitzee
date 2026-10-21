package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Expense
import com.kromanid.splitzee.models.ExpenseShare
import com.kromanid.splitzee.models.Household
import com.kromanid.splitzee.models.Member
import com.kromanid.splitzee.models.NewExpense
import com.kromanid.splitzee.models.Period
import com.kromanid.splitzee.repositories.ExpensesRepo
import com.kromanid.splitzee.repositories.HouseholdRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Instant

internal val home = Household("home", "Home", "EUR")

internal class FakeHouseholdRepo(vararg members: Member = arrayOf(Member("me", "Me"))) : HouseholdRepo {
    val members = MutableStateFlow(members.toList())

    override suspend fun current() = home

    override suspend fun addMember(householdId: String, name: String): Member {
        val member = Member("m${members.value.size}", name)
        members.update { it + member }
        return member
    }

    override fun observeMembers(householdId: String): Flow<List<Member>> = members
}

internal class FakeExpensesRepo : ExpensesRepo {
    val expenses = MutableStateFlow(emptyList<Expense>())

    override suspend fun addExpense(householdId: String, expense: NewExpense, shares: List<ExpenseShare>): Expense {
        val stored = with(expense) { Expense("e${expenses.value.size}", description, amount, category, date, paidBy, split, shares) }
        expenses.update { it + stored }
        return stored
    }

    override fun observeExpenses(householdId: String, period: Period): Flow<List<Expense>> =
        expenses.map { all -> all.filter { e -> period.range?.let { e.date in it } ?: true } }
}

internal class FixedClock(private val instant: Instant) : Clock {
    override fun now() = instant
}
