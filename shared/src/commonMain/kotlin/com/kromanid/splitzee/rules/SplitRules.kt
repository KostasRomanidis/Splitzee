package com.kromanid.splitzee.rules

import com.kromanid.splitzee.models.ExpenseError
import com.kromanid.splitzee.models.ExpenseShare
import com.kromanid.splitzee.models.MemberId
import com.kromanid.splitzee.models.Money
import com.kromanid.splitzee.models.SplitRule

private const val FULL_BP = 10_000

internal val SplitRule.participants: List<MemberId>
    get() = when (this) {
        is SplitRule.Equal -> participants
        is SplitRule.Exact -> shares.map { it.first }
        is SplitRule.Percent -> shares.map { it.first }
    }

/** Turns [rule] into per-member shares that sum exactly to [amount], or every rule violation. */
internal fun computeShares(amount: Money, payer: MemberId, rule: SplitRule): Validated<List<ExpenseShare>> {
    val participants = rule.participants
    if (participants.isEmpty()) return Validated.Invalid(listOf(ExpenseError.NoParticipants))

    val errors = participants.groupingBy { it }.eachCount()
        .filterValues { it > 1 }.keys
        .map { ExpenseError.DuplicateParticipant(it) }
        .toMutableList<ExpenseError>()

    val floored: List<Long> = when (rule) {
        is SplitRule.Equal -> List(participants.size) { amount.minor / participants.size }
        is SplitRule.Exact -> {
            rule.shares.filter { it.second.minor <= 0 }.mapTo(errors) { ExpenseError.NonPositiveShare(it.first) }
            val difference = amount.minor - rule.shares.sumOf { it.second.minor }
            if (difference != 0L) errors += ExpenseError.SumMismatch(Money(difference))
            rule.shares.map { it.second.minor }
        }
        is SplitRule.Percent -> {
            rule.shares.filter { it.second <= 0 }.mapTo(errors) { ExpenseError.NonPositiveShare(it.first) }
            val difference = FULL_BP - rule.shares.sumOf { it.second }
            if (difference != 0) errors += ExpenseError.PercentSumMismatch(difference)
            rule.shares.map { amount.minor * it.second / FULL_BP }
        }
    }
    if (errors.isNotEmpty()) return Validated.Invalid(errors)

    // Leftover minor units all go to one person, so every other participant's share stays the floored amount.
    val remainder = amount.minor - floored.sum()
    val absorber = if (payer in participants) payer else participants.first()
    return Validated.Valid(participants.mapIndexed { i, member ->
        ExpenseShare(member, Money(floored[i] + if (member == absorber) remainder else 0))
    })
}
