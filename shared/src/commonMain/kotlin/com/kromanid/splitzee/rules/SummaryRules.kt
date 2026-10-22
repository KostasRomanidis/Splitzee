package com.kromanid.splitzee.rules

import com.kromanid.splitzee.models.Category
import com.kromanid.splitzee.models.CategoryTotal
import com.kromanid.splitzee.models.Expense
import com.kromanid.splitzee.models.Member
import com.kromanid.splitzee.models.MemberSummary
import com.kromanid.splitzee.models.Money
import com.kromanid.splitzee.models.Summary

/**
 * Aggregates [expenses] (already filtered to a period) into totals. Every member gets a row, even with no activity;
 * a [removed] member gets one only when they paid or share in one of [expenses].
 */
internal fun summarize(members: List<Member>, removed: List<Member>, expenses: List<Expense>): Summary {
    // Custom names group ignoring case and whitespace; the first name seen is the one shown.
    val byCategory = expenses.groupBy { (it.category as? Category.Custom)?.name?.trim()?.lowercase() ?: it.category }
        .values
        .map { group -> CategoryTotal(group.first().category, group.sumMoney { it.amount }) }

    val involved = expenses.flatMapTo(mutableSetOf()) { e -> e.shares.map { it.memberId } + e.paidBy }

    return Summary(
        total = expenses.sumMoney { it.amount },
        byCategory = byCategory,
        members = (members + removed.filter { it.id in involved }).map { member ->
            MemberSummary(
                member = member,
                paid = expenses.filter { it.paidBy == member.id }.sumMoney { it.amount },
                share = expenses.flatMap { it.shares }.filter { it.memberId == member.id }.sumMoney { it.amount },
            )
        },
    )
}

private inline fun <T> List<T>.sumMoney(selector: (T) -> Money) = Money(sumOf { selector(it).minor })
