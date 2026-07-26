package com.harissabil.fisch.core.billing.di

import android.app.Application
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.harissabil.fisch.core.billing.data.BillingManagerImpl
import com.harissabil.fisch.core.billing.domain.BillingManager
import com.harissabil.fisch.core.firebase.firestore.domain.usecase.UpdateUserPlan
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun providesBillingManager(
        application: Application,
        updateUserPlan: UpdateUserPlan,
    ): BillingManager =
        BillingManagerImpl(
            application = application,
            updateUserPlan = updateUserPlan,
            auth = Firebase.auth,
        )
}
