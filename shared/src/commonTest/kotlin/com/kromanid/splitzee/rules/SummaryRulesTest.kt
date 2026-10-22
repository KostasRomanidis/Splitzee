package com.kromanid.splitzee.rules

import com.kromanid.splitzee.models.Category
import com.kromanid.splitzee.models.CategoryTotal
import com.kromanid.splitzee.models.Expense
import com.kromanid.splitzee.models.FixedCategory
import com.kromanid.splitzee.models.Member
import com.kromanid.splitzee.models.MemberSummary
import com.kromanid.splitzee.models.Money
import com.kromanid.splitzee.models.SplitRule
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class SummaryRulesTest {
    private val ana = Member("ana", "Ana")
    private val ben = Member("ben", "Ben")
    private val cleo = Member("cleo", "Cleo")
    private val members = listOf(ana, ben, cleo)

    private fun expense(
        amount: Long,
        paidBy: Member = ana,
        split: SplitRule = SplitRule.Equal(listOf("ana", "ben")),
        category: Category = Category.Fixed(FixedCategory.Groceries),
    ): Expense {
        val shares = (computeShares(Money(amount), paidBy.id, split) as Validated.Valid).value
        return Expense("e$amount", "Expense", Money(amount), category, LocalDate(2026, 10, 1), paidBy.id, split, shares)
    }

    @Test
    fun total() {
        assertEquals(Money(5750), summarize(members, emptyList(), listOf(expense(4500), expense(1250))).total)
    }

    @Test
    fun emptyPeriod() {
        val summary = summarize(members, emptyList(), emptyList())
        assertEquals(Money.ZERO, summary.total)
        assertEquals(emptyList(), summary.byCategory)
        assertEquals(members.map { MemberSummary(it, Money.ZERO, Money.ZERO) }, summary.members)
        summary.members.forEach { assertEquals(Money.ZERO, it.balance) }
    }

    @Test
    fun groupsCustomNamesIgnoringCaseAndWhitespace() {
        val summary = summarize(
            members,
            emptyList(),
            listOf(
                expense(1000, category = Category.Custom("Pet food")),
                expense(500, category = Category.Custom(" pet food")),
                expense(300),
            ),
        )
        assertEquals(
            listOf(
                CategoryTotal(Category.Custom("Pet food"), Money(1500)),
                CategoryTotal(Category.Fixed(FixedCategory.Groceries), Money(300)),
            ),
            summary.byCategory,
        )
    }

    @Test
    fun paidAndShare() {
        val summary = summarize(members, emptyList(), listOf(expense(4500)))
        assertEquals(
            listOf(
                MemberSummary(ana, Money(4500), Money(2250)),
                MemberSummary(ben, Money.ZERO, Money(2250)),
                MemberSummary(cleo, Money.ZERO, Money.ZERO),
            ),
            summary.members,
        )
        assertEquals(Money(2250), summary.members[0].balance)
        assertEquals(Money(-2250), summary.members[1].balance)
    }

    @Test
    fun balancesNetToZero() {
        val summary = summarize(
            members,
            emptyList(),
            listOf(
                expense(1000, paidBy = ana, split = SplitRule.Equal(listOf("ben", "ana", "cleo"))),
                expense(5, paidBy = ben, split = SplitRule.Equal(listOf("ana", "ben", "cleo"))),
                expense(2000, paidBy = cleo, split = SplitRule.Equal(listOf("ben"))),
                expense(5000, paidBy = ana, split = SplitRule.Exact(listOf("ben" to Money(3100), "cleo" to Money(1900)))),
                expense(999, paidBy = ben, split = SplitRule.Percent(listOf("ana" to 3333, "ben" to 3333, "cleo" to 3334))),
            ),
        )
        assertEquals(Money.ZERO, Money(summary.members.sumOf { it.balance.minor }))
    }

    @Test
    fun removedPayerKeepsFiguresAndBalancesNet() {
        val dan = Member("dan", "Dan")
        val summary = summarize(members, listOf(dan), listOf(expense(4500, paidBy = dan, split = SplitRule.Equal(listOf("dan", "ben")))))

        assertEquals(MemberSummary(dan, Money(4500), Money(2250)), summary.members.last())
        assertEquals(4, summary.members.size)
        assertEquals(Money.ZERO, Money(summary.members.sumOf { it.balance.minor }))
    }

    @Test
    fun removedMemberWithoutExpensesIsAbsent() {
        val summary = summarize(members, listOf(Member("dan", "Dan")), listOf(expense(4500)))

        assertEquals(members, summary.members.map { it.member })
    }
}
