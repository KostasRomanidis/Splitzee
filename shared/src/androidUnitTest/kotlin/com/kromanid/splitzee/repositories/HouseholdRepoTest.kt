package com.kromanid.splitzee.repositories

import com.kromanid.splitzee.database.SplitzeeDatabase
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HouseholdRepoTest {
    private val driver = inMemoryDriver()
    private val repo = HouseholdRepoImpl(SplitzeeDatabase(driver), TickingClock())

    @Test
    fun emptyDatabaseHasNoHousehold() = runBlocking {
        assertNull(repo.current())
        assertEquals(0L, driver.count("household"))
    }

    @Test
    fun createSelectsHouseholdWithOwner() = runBlocking {
        val household = repo.createHousehold("Home")

        assertEquals(household, repo.current())
        assertEquals("EUR", household.currency)
        assertEquals(listOf("Me"), repo.observeMembers(household.id).first().map { it.name })
    }

    @Test
    fun selectionSwitchesAndPersists() = runBlocking {
        val home = repo.createHousehold("Home")
        repo.createHousehold("Beach house")

        repo.selectHousehold(home.id)

        assertEquals(home, repo.current())
        assertEquals(home, HouseholdRepoImpl(SplitzeeDatabase(driver), TickingClock()).current())
    }

    @Test
    fun deleteFallsBackToMostRecentlySelected() = runBlocking {
        val flat = repo.createHousehold("Flat")
        val home = repo.createHousehold("Home")
        val beach = repo.createHousehold("Beach house")
        repo.selectHousehold(flat.id)
        repo.selectHousehold(beach.id)

        repo.deleteHousehold(beach.id)
        assertEquals(flat, repo.current())
        assertEquals(listOf(flat, home), repo.observeHouseholds().first())

        repo.deleteHousehold(flat.id)
        repo.deleteHousehold(home.id)
        assertNull(repo.current())
    }

    @Test
    fun observeCurrentFollowsCreateSelectDelete() = runBlocking {
        val updates = Channel<String?>(Channel.UNLIMITED)
        val observer = launch { repo.observeCurrent().collect { updates.send(it?.name) } }
        assertNull(withTimeout(5_000) { updates.receive() })

        val home = repo.createHousehold("Home")
        assertEquals("Home", withTimeout(5_000) { updates.receive() })
        val beach = repo.createHousehold("Beach house")
        assertEquals("Beach house", withTimeout(5_000) { updates.receive() })
        repo.selectHousehold(home.id)
        assertEquals("Home", withTimeout(5_000) { updates.receive() })
        repo.deleteHousehold(home.id)
        assertEquals("Beach house", withTimeout(5_000) { updates.receive() })
        repo.deleteHousehold(beach.id)
        assertNull(withTimeout(5_000) { updates.receive() })
        observer.cancel()
    }

    @Test
    fun sameNameMembersGetDistinctIds() = runBlocking {
        val household = repo.createHousehold("Home")
        val a = repo.addMember(household.id, "Ana")
        val b = repo.addMember(household.id, "Ana")

        assertNotEquals(a.id, b.id)
        assertEquals(listOf("Me", "Ana", "Ana"), repo.observeMembers(household.id).first().map { it.name })
    }

    @Test
    fun observerSeesAddedMember() = runBlocking {
        val household = repo.createHousehold("Home")
        // Undispatched so the query listener is registered before the insert.
        val updates = async(start = CoroutineStart.UNDISPATCHED) { repo.observeMembers(household.id).take(2).toList() }
        repo.addMember(household.id, "Ana")

        assertEquals(listOf("Me", "Ana"), withTimeout(5_000) { updates.await() }.last().map { it.name })
    }

    @Test
    fun removedMemberMovesToRemovedList() = runBlocking {
        val household = repo.createHousehold("Home")
        val ana = repo.addMember(household.id, "Ana")

        assertTrue(repo.removeMember(household.id, ana.id))

        assertEquals(listOf("Me"), repo.observeMembers(household.id).first().map { it.name })
        assertEquals(listOf(ana), repo.observeRemovedMembers(household.id).first())
    }

    @Test
    fun ownerCannotBeRemoved() = runBlocking {
        val household = repo.createHousehold("Home")
        val me = repo.observeMembers(household.id).first().single()

        assertFalse(repo.removeMember(household.id, me.id))
        assertEquals(listOf(me), repo.observeMembers(household.id).first())
    }
}
