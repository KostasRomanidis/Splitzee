package com.kromanid.splitzee.rules

import com.kromanid.splitzee.models.ExpenseError
import com.kromanid.splitzee.models.ExpenseShare
import com.kromanid.splitzee.models.Money
import com.kromanid.splitzee.models.SplitRule
import kotlin.test.Test
import kotlin.test.assertEquals

private const val ANA = "ana"
private const val BEN = "ben"
private const val CLEO = "cleo"
private const val DAN = "dan"

class SplitRulesTest {
    private fun valid(vararg shares: Pair<String, Long>) =
        Validated.Valid(shares.map { ExpenseShare(it.first, Money(it.second)) })

    private fun invalid(vararg errors: ExpenseError) = Validated.Invalid(errors.toList())

    @Test
    fun equalDividesEvenly() {
        assertEquals(
            valid(ANA to 1000, BEN to 1000, CLEO to 1000),
            computeShares(Money(3000), ANA, SplitRule.Equal(listOf(ANA, BEN, CLEO))),
        )
    }

    @Test
    fun singleParticipantOwesEverything() {
        assertEquals(valid(BEN to 4000), computeShares(Money(4000), ANA, SplitRule.Equal(listOf(BEN))))
    }

    @Test
    fun payerAbsorbsLeftoverCent() {
        assertEquals(
            valid(BEN to 333, ANA to 334, CLEO to 333),
            computeShares(Money(1000), ANA, SplitRule.Equal(listOf(BEN, ANA, CLEO))),
        )
    }

    @Test
    fun payerAbsorbsSeveralLeftoverCents() {
        assertEquals(
            valid(ANA to 3, BEN to 1, CLEO to 1),
            computeShares(Money(5), ANA, SplitRule.Equal(listOf(ANA, BEN, CLEO))),
        )
    }

    @Test
    fun firstParticipantAbsorbsWhenPayerNotParticipating() {
        assertEquals(
            valid(BEN to 334, CLEO to 333, DAN to 333),
            computeShares(Money(1000), ANA, SplitRule.Equal(listOf(BEN, CLEO, DAN))),
        )
    }

    @Test
    fun validExactSplit() {
        assertEquals(
            valid(ANA to 3000, BEN to 2000),
            computeShares(Money(5000), ANA, SplitRule.Exact(listOf(ANA to Money(3000), BEN to Money(2000)))),
        )
    }

    @Test
    fun exactSharesDoNotAddUp() {
        assertEquals(
            invalid(ExpenseError.SumMismatch(Money(500))),
            computeShares(Money(5000), ANA, SplitRule.Exact(listOf(ANA to Money(3000), BEN to Money(1500)))),
        )
    }

    @Test
    fun exactZeroShare() {
        assertEquals(
            invalid(ExpenseError.NonPositiveShare(BEN)),
            computeShares(Money(5000), ANA, SplitRule.Exact(listOf(ANA to Money(5000), BEN to Money(0)))),
        )
    }

    @Test
    fun percentagesDivideCleanly() {
        assertEquals(
            valid(ANA to 5000, BEN to 3000, CLEO to 2000),
            computeShares(Money(10000), ANA, SplitRule.Percent(listOf(ANA to 5000, BEN to 3000, CLEO to 2000))),
        )
    }

    @Test
    fun percentagesWithLeftover() {
        assertEquals(
            valid(ANA to 333, BEN to 333, CLEO to 334),
            computeShares(Money(1000), CLEO, SplitRule.Percent(listOf(ANA to 3333, BEN to 3333, CLEO to 3334))),
        )
    }

    @Test
    fun percentagesDoNotAddUp() {
        assertEquals(
            invalid(ExpenseError.PercentSumMismatch(1000)),
            computeShares(Money(1000), ANA, SplitRule.Percent(listOf(ANA to 6000, BEN to 3000))),
        )
    }

    @Test
    fun zeroPercentage() {
        assertEquals(
            invalid(ExpenseError.NonPositiveShare(BEN)),
            computeShares(Money(1000), ANA, SplitRule.Percent(listOf(ANA to 10000, BEN to 0))),
        )
    }

    @Test
    fun noParticipants() {
        assertEquals(invalid(ExpenseError.NoParticipants), computeShares(Money(1000), ANA, SplitRule.Equal(emptyList())))
    }

    @Test
    fun duplicateParticipant() {
        assertEquals(
            invalid(ExpenseError.DuplicateParticipant(ANA)),
            computeShares(Money(1000), ANA, SplitRule.Equal(listOf(ANA, BEN, ANA))),
        )
    }
}
