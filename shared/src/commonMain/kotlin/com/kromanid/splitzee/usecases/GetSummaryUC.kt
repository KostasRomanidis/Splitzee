package com.kromanid.splitzee.usecases

import com.kromanid.splitzee.models.Period
import com.kromanid.splitzee.models.Summary
import com.kromanid.splitzee.repositories.ExpensesRepo
import com.kromanid.splitzee.repositories.HouseholdRepo
import com.kromanid.splitzee.rules.summarize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

interface GetSummaryUC {
    /** The selected household's summary for [period], updated when members, expenses or the selection change. */
    fun execute(period: Period): Flow<Summary>
}

internal class GetSummaryUCImpl(
    private val householdRepo: HouseholdRepo,
    private val expensesRepo: ExpensesRepo,
) : GetSummaryUC {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun execute(period: Period): Flow<Summary> = householdRepo.observeCurrent().flatMapLatest { household ->
        if (household == null) return@flatMapLatest flowOf(summarize(emptyList(), emptyList(), emptyList()))
        combine(
            householdRepo.observeMembers(household.id),
            householdRepo.observeRemovedMembers(household.id),
            expensesRepo.observeExpenses(household.id, period),
            ::summarize,
        )
    }
}
