package com.kromanid.splitzee.models

// Ordered lists, not maps: the first participant takes the remainder when the payer does not participate.
sealed interface SplitRule {
    data class Equal(val participants: List<MemberId>) : SplitRule
    data class Exact(val shares: List<Pair<MemberId, Money>>) : SplitRule

    /** Percentages in basis points: 33.33% = 3333, total 10000. */
    data class Percent(val shares: List<Pair<MemberId, Int>>) : SplitRule
}
