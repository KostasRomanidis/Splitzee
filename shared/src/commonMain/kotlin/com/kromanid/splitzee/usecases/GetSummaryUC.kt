package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Period
import com.kromanid.splitzee.models.Summary
import com.kromanid.splitzee.repositories.ExpensesRepo
import com.kromanid.splitzee.repositories.HouseholdRepo
import com.kromanid.splitzee.rules.summarize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

interface GetSummaryUC {
    /** The current household's summary for [period], updated when members or expenses change. */
    fun execute(period: Period): Flow<Summary>
}

internal class GetSummaryUCImpl(
    private val householdRepo: HouseholdRepo,
    private val expensesRepo: ExpensesRepo,
) : GetSummaryUC {
    override fun execute(period: Period): Flow<Summary> = flow {
        val householdId = householdRepo.current().id
        emitAll(
            combine(householdRepo.observeMembers(householdId), expensesRepo.observeExpenses(householdId, period), ::summarize)
        )
    }
}
