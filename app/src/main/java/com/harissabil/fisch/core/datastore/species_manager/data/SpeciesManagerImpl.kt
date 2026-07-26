package com.harissabil.fisch.core.datastore.species_manager.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.harissabil.fisch.core.datastore.species_manager.domain.SpeciesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Stores previously logged fish species names on-device so add/edit-catch screens can suggest
 * them without querying Firestore (avoids opening yet another realtime listener just for
 * autocomplete, on top of the one the catches list already keeps open).
 */
class SpeciesManagerImpl @Inject constructor(
    private val context: Context,
) : SpeciesManager {

    override suspend fun addSpecies(species: String) {
        val trimmed = species.trim()
        if (trimmed.isEmpty()) return

        context.dataStore.edit { settings ->
            val current = settings[SpeciesKeys.KNOWN_SPECIES] ?: emptySet()
            settings[SpeciesKeys.KNOWN_SPECIES] = current + trimmed
        }
    }

    override fun readSpecies(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[SpeciesKeys.KNOWN_SPECIES] ?: emptySet()
        }
    }
}

private val readOnlyProperty = preferencesDataStore(name = "species")

private val Context.dataStore: DataStore<Preferences> by readOnlyProperty

private object SpeciesKeys {
    val KNOWN_SPECIES = stringSetPreferencesKey("known_species")
}
