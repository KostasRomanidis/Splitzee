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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Instant

internal val home = Household("home", "Home", "EUR")

/** Starts with [home] selected, holding [members]; the "me" member and created households' "Me" are owners. */
internal class FakeHouseholdRepo(vararg members: Member = arrayOf(Member("me", "Me"))) : HouseholdRepo {
    val households = MutableStateFlow(listOf(home))
    val selected = MutableStateFlow<Household?>(home)
    /** Active members by household id. */
    val members = MutableStateFlow(mapOf(home.id to members.toList()))
    /** Removed members by household id. */
    val removed = MutableStateFlow(emptyMap<String, List<Member>>())
    private val owners = mutableSetOf("me")

    override suspend fun current() = selected.value
    override fun observeCurrent(): Flow<Household?> = selected
    override fun observeHouseholds(): Flow<List<Household>> = households

    override suspend fun createHousehold(name: String): Household {
        val household = Household("h${households.value.size}", name, "EUR")
        val owner = Member("${household.id}-me", "Me")
        owners += owner.id
        households.update { it + household }
        members.update { it + (household.id to listOf(owner)) }
        selected.value = household
        return household
    }

    override suspend fun selectHousehold(id: String) {
        households.value.find { it.id == id }?.let { selected.value = it }
    }

    // shortcut: falls back to the last created survivor, not the most recently selected; the real repo is tested for that.
    override suspend fun deleteHousehold(id: String) {
        households.update { all -> all.filter { it.id != id } }
        if (selected.value?.id == id) selected.value = households.value.lastOrNull()
    }

    override suspend fun addMember(householdId: String, name: String): Member {
        val member = Member("m${members.value.values.sumOf { it.size }}", name)
        members.update { it + (householdId to it[householdId].orEmpty() + member) }
        return member
    }

    override suspend fun removeMember(householdId: String, memberId: String): Boolean {
        if (memberId in owners) return false
        val member = members.value[householdId].orEmpty().find { it.id == memberId } ?: return true
        members.update { it + (householdId to it.getValue(householdId) - member) }
        removed.update { it + (householdId to it[householdId].orEmpty() + member) }
        return true
    }

    override fun observeMembers(householdId: String): Flow<List<Member>> = members.map { it[householdId].orEmpty() }.distinctUntilChanged()
    override fun observeRemovedMembers(householdId: String): Flow<List<Member>> = removed.map { it[householdId].orEmpty() }.distinctUntilChanged()
}

internal class FakeExpensesRepo : ExpensesRepo {
    val expenses = MutableStateFlow(emptyList<Expense>())
    private val householdOf = mutableMapOf<String, String>()

    override suspend fun addExpense(householdId: String, expense: NewExpense, shares: List<ExpenseShare>): Expense {
        val stored = with(expense) { Expense("e${expenses.value.size}", description, amount, category, date, paidBy, split, shares) }
        householdOf[stored.id] = householdId
        expenses.update { it + stored }
        return stored
    }

    override fun observeExpenses(householdId: String, period: Period): Flow<List<Expense>> =
        expenses.map { all ->
            all.filter { e -> householdOf[e.id] == householdId && period.range?.let { e.date in it } ?: true }
        }
}

internal class FixedClock(private val instant: Instant) : Clock {
    override fun now() = instant
}
