package com.kromanid.splitzee.di

import com.kromanid.splitzee.usecases.AddExpenseUC
import com.kromanid.splitzee.usecases.CreateHouseholdUC
import com.kromanid.splitzee.usecases.DeleteHouseholdUC
import com.kromanid.splitzee.usecases.GetCurrentHouseholdUC
import com.kromanid.splitzee.usecases.GetExpensesUC
import com.kromanid.splitzee.usecases.GetHouseholdsUC
import com.kromanid.splitzee.usecases.RemoveMemberUC
import com.kromanid.splitzee.usecases.SelectHouseholdUC
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class UseCaseHelper : KoinComponent {
    fun getAddExpenseUC(): AddExpenseUC = get()
    fun getExpensesUC(): GetExpensesUC = get()
    fun getCreateHouseholdUC(): CreateHouseholdUC = get()
    fun getSelectHouseholdUC(): SelectHouseholdUC = get()
    fun getDeleteHouseholdUC(): DeleteHouseholdUC = get()
    fun getHouseholdsUC(): GetHouseholdsUC = get()
    fun getCurrentHouseholdUC(): GetCurrentHouseholdUC = get()
    fun getRemoveMemberUC(): RemoveMemberUC = get()
}
