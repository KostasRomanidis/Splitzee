package com.kromanid.splitzee.sources

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val USER_EXPENSES_PREFERENCE_NAME = "user_expenses"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_EXPENSES_PREFERENCE_NAME)


actual interface ExpensesSource {
    actual suspend fun save(key: String, value: Long)
    actual fun get(): Flow<Map<String, Long>>
}

class ExpensesSourceImpl(
    private val context: Context
) : ExpensesSource {

    private val dataStore: DataStore<Preferences> = context.dataStore

    override suspend fun save(key: String, value: Long) {
        dataStore.edit { prefs ->
            prefs[longPreferencesKey(key)] = value
        }
    }

    override fun get(): Flow<Map<String, Long>> {
        return dataStore.data.map { preferences ->
            preferences.asMap().mapNotNull { (key, value) ->
                (value as? Long)?.let { key.name to it }
            }.toMap()
        }
    }
}

