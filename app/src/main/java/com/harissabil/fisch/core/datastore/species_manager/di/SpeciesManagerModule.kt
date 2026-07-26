package com.harissabil.fisch.core.datastore.species_manager.di

import android.content.Context
import com.harissabil.fisch.core.datastore.species_manager.data.SpeciesManagerImpl
import com.harissabil.fisch.core.datastore.species_manager.domain.SpeciesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SpeciesManagerModule {

    @Provides
    @Singleton
    fun provideSpeciesManager(@ApplicationContext context: Context): SpeciesManager {
        return SpeciesManagerImpl(context = context)
    }
}
