package com.kromanid.splitzee.di

import com.kromanid.splitzee.repositories.ExpensesRepo
import com.kromanid.splitzee.repositories.ExpensesRepoImpl
import com.kromanid.splitzee.repositories.HouseholdRepo
import com.kromanid.splitzee.repositories.HouseholdRepoImpl
import com.kromanid.splitzee.usecases.AddExpenseUC
import com.kromanid.splitzee.usecases.AddExpenseUCImpl
import com.kromanid.splitzee.usecases.AddMemberUC
import com.kromanid.splitzee.usecases.AddMemberUCImpl
import com.kromanid.splitzee.usecases.CreateHouseholdUC
import com.kromanid.splitzee.usecases.CreateHouseholdUCImpl
import com.kromanid.splitzee.usecases.DeleteHouseholdUC
import com.kromanid.splitzee.usecases.DeleteHouseholdUCImpl
import com.kromanid.splitzee.usecases.GetCurrentHouseholdUC
import com.kromanid.splitzee.usecases.GetCurrentHouseholdUCImpl
import com.kromanid.splitzee.usecases.GetExpensesUC
import com.kromanid.splitzee.usecases.GetExpensesUCImpl
import com.kromanid.splitzee.usecases.GetHouseholdsUC
import com.kromanid.splitzee.usecases.GetHouseholdsUCImpl
import com.kromanid.splitzee.usecases.GetMembersUC
import com.kromanid.splitzee.usecases.GetMembersUCImpl
import com.kromanid.splitzee.usecases.GetSummaryUC
import com.kromanid.splitzee.usecases.GetSummaryUCImpl
import com.kromanid.splitzee.usecases.RemoveMemberUC
import com.kromanid.splitzee.usecases.RemoveMemberUCImpl
import com.kromanid.splitzee.usecases.SelectHouseholdUC
import com.kromanid.splitzee.usecases.SelectHouseholdUCImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

val commonModules = module {
    single<Clock> { Clock.System }

    // usecases
    factoryOf(::AddExpenseUCImpl).bind<AddExpenseUC>()
    factoryOf(::GetExpensesUCImpl).bind<GetExpensesUC>()
    factoryOf(::AddMemberUCImpl).bind<AddMemberUC>()
    factoryOf(::GetMembersUCImpl).bind<GetMembersUC>()
    factoryOf(::GetSummaryUCImpl).bind<GetSummaryUC>()
    factoryOf(::RemoveMemberUCImpl).bind<RemoveMemberUC>()
    factoryOf(::CreateHouseholdUCImpl).bind<CreateHouseholdUC>()
    factoryOf(::SelectHouseholdUCImpl).bind<SelectHouseholdUC>()
    factoryOf(::DeleteHouseholdUCImpl).bind<DeleteHouseholdUC>()
    factoryOf(::GetHouseholdsUCImpl).bind<GetHouseholdsUC>()
    factoryOf(::GetCurrentHouseholdUCImpl).bind<GetCurrentHouseholdUC>()

    // repositories
    singleOf(::HouseholdRepoImpl).bind<HouseholdRepo>()
    singleOf(::ExpensesRepoImpl).bind<ExpensesRepo>()
}
