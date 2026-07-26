package com.harissabil.fisch.core.datastore.species_manager.domain

import kotlinx.coroutines.flow.Flow

interface SpeciesManager {

    suspend fun addSpecies(species: String)

    fun readSpecies(): Flow<Set<String>>
}
