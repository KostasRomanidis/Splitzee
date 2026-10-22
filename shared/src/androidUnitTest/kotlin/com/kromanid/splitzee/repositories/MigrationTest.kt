package com.kromanid.splitzee.repositories

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kromanid.splitzee.database.SplitzeeDatabase
import com.kromanid.splitzee.models.Household
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MigrationTest {
    // Schema version 1 as generated before the creator column; tests run from the module directory.
    private val dbFile = File.createTempFile("splitzee-v1", ".db").also {
        File("src/commonMain/sqldelight/databases/1.db").copyTo(it, overwrite = true)
    }
    private val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.path}")

    @AfterTest
    fun cleanUp() {
        driver.close()
        dbFile.delete()
    }

    @Test
    fun existingHouseholdIsAttributedToLocalUser() = runBlocking {
        driver.execute(null, "INSERT INTO household(id, name, currency, created_at, updated_at, selected_at) VALUES ('home', 'Home', 'EUR', 1, 1, 1)", 0)
        driver.execute(null, "INSERT INTO member(id, household_id, name, is_owner, created_at, updated_at) VALUES ('me', 'home', 'Me', 1, 1, 1)", 0)

        SplitzeeDatabase.Schema.migrate(driver, 1, SplitzeeDatabase.Schema.version)
        val repo = HouseholdRepoImpl(SplitzeeDatabase(driver), TickingClock())

        val home = Household("home", "Home", "EUR", isCreator = true)
        assertEquals(listOf(home), repo.observeHouseholds().first())
        assertEquals(listOf("Me"), repo.observeMembers(home.id).first().map { it.name })
        assertTrue(repo.deleteHousehold(home.id))
        assertNull(repo.current())
    }
}
