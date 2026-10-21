package com.kromanid.splitzee.repositories

import com.kromanid.splitzee.database.SplitzeeDatabase
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class HouseholdRepoTest {
    private val driver = inMemoryDriver()
    private val repo = HouseholdRepoImpl(SplitzeeDatabase(driver), TickingClock())

    @Test
    fun seedsDefaultHouseholdOnce() = runBlocking {
        val first = repo.current()
        val second = repo.current()

        assertEquals(first, second)
        assertEquals("Home", first.name)
        assertEquals("EUR", first.currency)
        assertEquals(listOf("Me"), repo.observeMembers(first.id).first().map { it.name })
        assertEquals(1L, driver.count("household"))
    }

    @Test
    fun sameNameMembersGetDistinctIds() = runBlocking {
        val household = repo.current()
        val a = repo.addMember(household.id, "Ana")
        val b = repo.addMember(household.id, "Ana")

        assertNotEquals(a.id, b.id)
        assertEquals(listOf("Me", "Ana", "Ana"), repo.observeMembers(household.id).first().map { it.name })
    }

    @Test
    fun observerSeesAddedMember() = runBlocking {
        val household = repo.current()
        // Undispatched so the query listener is registered before the insert.
        val updates = async(start = CoroutineStart.UNDISPATCHED) { repo.observeMembers(household.id).take(2).toList() }
        repo.addMember(household.id, "Ana")

        assertEquals(listOf("Me", "Ana"), withTimeout(5_000) { updates.await() }.last().map { it.name })
    }
}
