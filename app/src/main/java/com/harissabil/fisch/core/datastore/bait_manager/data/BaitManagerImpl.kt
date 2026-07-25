package com.harissabil.fisch.core.datastore.bait_manager.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.harissabil.fisch.core.datastore.bait_manager.domain.BaitManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Stores previously logged bait/lure names on-device so add/edit-catch screens can suggest
 * them without querying Firestore (avoids opening yet another realtime listener just for
 * autocomplete, on top of the one the catches list already keeps open).
 */
class BaitManagerImpl @Inject constructor(
    private val context: Context,
) : BaitManager {

    override suspend fun addBait(bait: String) {
        val trimmed = bait.trim()
        if (trimmed.isEmpty()) return

        context.dataStore.edit { settings ->
            val current = settings[BaitKeys.KNOWN_BAITS] ?: emptySet()
            settings[BaitKeys.KNOWN_BAITS] = current + trimmed
        }
    }

    override fun readBaits(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[BaitKeys.KNOWN_BAITS] ?: emptySet()
        }
    }
}

private val readOnlyProperty = preferencesDataStore(name = "baits")

private val Context.dataStore: DataStore<Preferences> by readOnlyProperty

private object BaitKeys {
    val KNOWN_BAITS = stringSetPreferencesKey("known_baits")
}
