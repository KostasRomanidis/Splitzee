package com.kromanid.splitzee.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
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
    /** The selected household, or null when none exists. */
    suspend fun current(): Household?
    fun observeCurrent(): Flow<Household?>
    fun observeHouseholds(): Flow<List<Household>>

    /** Creates a household with an owner "Me" member and selects it. */
    suspend fun createHousehold(name: String): Household
    suspend fun selectHousehold(id: String)
    /** Soft-deletes the household; false when the local user did not create it. */
    suspend fun deleteHousehold(id: String): Boolean

    suspend fun addMember(householdId: String, name: String): Member

    /** Soft-deletes the member; false when it is the household owner. */
    suspend fun removeMember(householdId: String, memberId: String): Boolean
    fun observeMembers(householdId: String): Flow<List<Member>>
    fun observeRemovedMembers(householdId: String): Flow<List<Member>>
}

internal class HouseholdRepoImpl(private val db: SplitzeeDatabase, private val clock: Clock) : HouseholdRepo {
    private fun now() = clock.now().toEpochMilliseconds()

    override suspend fun current(): Household? = withContext(Dispatchers.IO) {
        db.householdQueries.selectCurrent(::Household).executeAsOneOrNull()
    }

    override fun observeCurrent(): Flow<Household?> =
        db.householdQueries.selectCurrent(::Household).asFlow().mapToOneOrNull(Dispatchers.IO)

    override fun observeHouseholds(): Flow<List<Household>> =
        db.householdQueries.selectAll(::Household).asFlow().mapToList(Dispatchers.IO)

    override suspend fun createHousehold(name: String): Household = withContext(Dispatchers.IO) {
        val now = now()
        val household = Household(Uuid.random().toString(), name, currency = "EUR", isCreator = true)
        db.transaction {
            db.householdQueries.insert(household.id, household.name, household.currency, now, now, now)
            db.memberQueries.insert(Uuid.random().toString(), household.id, "Me", 1, now, now)
        }
        household
    }

    override suspend fun selectHousehold(id: String) = withContext(Dispatchers.IO) {
        db.householdQueries.select(now(), id)
        Unit
    }

    override suspend fun deleteHousehold(id: String): Boolean = withContext(Dispatchers.IO) {
        db.householdQueries.softDelete(now(), id).value > 0
    }

    override suspend fun addMember(householdId: String, name: String): Member = withContext(Dispatchers.IO) {
        val now = now()
        val member = Member(Uuid.random().toString(), name)
        db.memberQueries.insert(member.id, householdId, member.name, 0, now, now)
        member
    }

    override suspend fun removeMember(householdId: String, memberId: String): Boolean = withContext(Dispatchers.IO) {
        db.transactionWithResult {
            if (db.memberQueries.selectIsOwner(householdId, memberId).executeAsOneOrNull() == 1L) return@transactionWithResult false
            db.memberQueries.softDelete(now(), householdId, memberId)
            true
        }
    }

    override fun observeMembers(householdId: String): Flow<List<Member>> =
        db.memberQueries.selectByHousehold(householdId, ::Member).asFlow().mapToList(Dispatchers.IO)

    override fun observeRemovedMembers(householdId: String): Flow<List<Member>> =
        db.memberQueries.selectRemovedByHousehold(householdId, ::Member).asFlow().mapToList(Dispatchers.IO)
}
