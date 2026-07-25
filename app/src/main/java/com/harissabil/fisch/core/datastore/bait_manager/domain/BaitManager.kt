package com.harissabil.fisch.core.datastore.bait_manager.domain

import kotlinx.coroutines.flow.Flow

interface BaitManager {

    suspend fun addBait(bait: String)

    fun readBaits(): Flow<Set<String>>
}
