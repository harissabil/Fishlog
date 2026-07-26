package com.harissabil.fisch.core.datastore.bait_manager.di

import android.content.Context
import com.harissabil.fisch.core.datastore.bait_manager.data.BaitManagerImpl
import com.harissabil.fisch.core.datastore.bait_manager.domain.BaitManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BaitManagerModule {

    @Provides
    @Singleton
    fun provideBaitManager(@ApplicationContext context: Context): BaitManager {
        return BaitManagerImpl(context = context)
    }
}
