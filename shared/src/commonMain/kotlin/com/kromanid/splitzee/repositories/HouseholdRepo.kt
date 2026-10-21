package com.kromanid.splitzee.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.kromanid.splitzee.database.SplitzeeDatabase
import com.kromanid.splitzee.models.Household
import com.kromanid.splitzee.models.Member
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.uuid.Uuid

interface HouseholdRepo {
    /** The device's household, created with a "Me" member on first call. */
    suspend fun current(): Household
    suspend fun addMember(householdId: String, name: String): Member
    fun observeMembers(householdId: String): Flow<List<Member>>
}

internal class HouseholdRepoImpl(private val db: SplitzeeDatabase, private val clock: Clock) : HouseholdRepo {
    override suspend fun current(): Household = withContext(Dispatchers.IO) {
        db.transactionWithResult {
            db.householdQueries.selectCurrent(::Household).executeAsOneOrNull() ?: run {
                val now = clock.now().toEpochMilliseconds()
                val household = Household(Uuid.random().toString(), name = "Home", currency = "EUR")
                db.householdQueries.insert(household.id, household.name, household.currency, now, now)
                db.memberQueries.insert(Uuid.random().toString(), household.id, "Me", now, now)
                household
            }
        }
    }

    override suspend fun addMember(householdId: String, name: String): Member = withContext(Dispatchers.IO) {
        val now = clock.now().toEpochMilliseconds()
        val member = Member(Uuid.random().toString(), name)
        db.memberQueries.insert(member.id, householdId, member.name, now, now)
        member
    }

    override fun observeMembers(householdId: String): Flow<List<Member>> =
        db.memberQueries.selectByHousehold(householdId, ::Member).asFlow().mapToList(Dispatchers.IO)
}
