package com.kromanid.splitzee.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kromanid.splitzee.database.SplitzeeDatabase
import com.kromanid.splitzee.models.Category
import com.kromanid.splitzee.models.Expense
import com.kromanid.splitzee.models.ExpenseShare
import com.kromanid.splitzee.models.FixedCategory
import com.kromanid.splitzee.models.Money
import com.kromanid.splitzee.models.NewExpense
import com.kromanid.splitzee.models.Period
import com.kromanid.splitzee.models.SplitRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.uuid.Uuid

interface ExpensesRepo {
    /** Stores [expense] and its computed [shares] in one transaction. */
    suspend fun addExpense(householdId: String, expense: NewExpense, shares: List<ExpenseShare>): Expense

    /** Expenses with their shares, newest date first, then newest created first. */
    fun observeExpenses(householdId: String, period: Period = Period.AllTime): Flow<List<Expense>>
}

internal class ExpensesRepoImpl(private val db: SplitzeeDatabase, private val clock: Clock) : ExpensesRepo {
    override suspend fun addExpense(householdId: String, expense: NewExpense, shares: List<ExpenseShare>): Expense =
        withContext(Dispatchers.IO) {
            val now = clock.now().toEpochMilliseconds()
            val id = Uuid.random().toString()
            val (kind, value) = when (val c = expense.category) {
                is Category.Fixed -> FIXED to c.category.name
                is Category.Custom -> CUSTOM to c.name
            }
            val percents = (expense.split as? SplitRule.Percent)?.shares?.toMap()
            db.transaction {
                db.expenseQueries.insert(
                    id, householdId, expense.description, expense.amount.minor, kind, value,
                    expense.date.toString(), expense.paidBy, expense.split.type, now, now,
                )
                shares.forEachIndexed { position, share ->
                    db.expenseShareQueries.insert(
                        Uuid.random().toString(), id, share.memberId, share.amount.minor,
                        percents?.get(share.memberId)?.toLong(), position.toLong(), now, now,
                    )
                }
            }
            with(expense) { Expense(id, description, amount, category, date, paidBy, split, shares) }
        }

    override fun observeExpenses(householdId: String, period: Period): Flow<List<Expense>> {
        val range = period.range
        val query = if (range == null) db.expenseQueries.selectWithShares(householdId, ::ShareRow)
        else db.expenseQueries.selectInRangeWithShares(householdId, range.start.toString(), range.endExclusive.toString(), ::ShareRow)
        return query.asFlow().mapToList(Dispatchers.IO).map(::toExpenses)
    }

    private val SplitRule.type
        get() = when (this) {
            is SplitRule.Equal -> EQUAL
            is SplitRule.Exact -> EXACT
            is SplitRule.Percent -> PERCENT
        }

    /** One row per share, ordered by expense then share position. */
    private data class ShareRow(
        val id: String,
        val description: String,
        val amountMinor: Long,
        val categoryKind: String,
        val categoryValue: String,
        val date: String,
        val paidBy: String,
        val splitType: String,
        val memberId: String,
        val shareMinor: Long,
        val percentBp: Long?,
    )

    private fun toExpenses(rows: List<ShareRow>): List<Expense> = rows.groupBy { it.id }.values.map { group ->
        val e = group.first()
        val shares = group.map { ExpenseShare(it.memberId, Money(it.shareMinor)) }
        Expense(
            id = e.id,
            description = e.description,
            amount = Money(e.amountMinor),
            category = if (e.categoryKind == FIXED) Category.Fixed(FixedCategory.valueOf(e.categoryValue))
            else Category.Custom(e.categoryValue),
            date = LocalDate.parse(e.date),
            paidBy = e.paidBy,
            split = when (e.splitType) {
                EQUAL -> SplitRule.Equal(shares.map { it.memberId })
                EXACT -> SplitRule.Exact(shares.map { it.memberId to it.amount })
                else -> SplitRule.Percent(group.map { it.memberId to it.percentBp!!.toInt() })
            },
            shares = shares,
        )
    }

    private companion object {
        const val FIXED = "FIXED"
        const val CUSTOM = "CUSTOM"
        const val EQUAL = "EQUAL"
        const val EXACT = "EXACT"
        const val PERCENT = "PERCENT"
    }
}
