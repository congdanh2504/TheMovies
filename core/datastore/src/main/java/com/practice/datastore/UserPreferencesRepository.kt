package com.practice.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPreferences(val selectedHomeTab: Int = 0)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(selectedHomeTab = prefs[SELECTED_TAB_KEY] ?: 0)
    }

    suspend fun setSelectedHomeTab(index: Int) {
        context.dataStore.edit { it[SELECTED_TAB_KEY] = index }
    }

    companion object {
        private val SELECTED_TAB_KEY = intPreferencesKey("selected_tab")
    }
}
