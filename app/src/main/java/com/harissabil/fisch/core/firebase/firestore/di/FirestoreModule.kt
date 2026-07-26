package com.harissabil.fisch.core.firebase.firestore.di

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.harissabil.fisch.core.firebase.firestore.data.FirestoreRepositoryImpl
import com.harissabil.fisch.core.firebase.firestore.data.UserPlanRepositoryImpl
import com.harissabil.fisch.core.firebase.firestore.domain.FirestoreRepository
import com.harissabil.fisch.core.firebase.firestore.domain.UserPlanRepository
import com.harissabil.fisch.core.firebase.firestore.domain.model.Constant.LOGBOOKS
import com.harissabil.fisch.core.firebase.firestore.domain.model.Constant.MAPS
import com.harissabil.fisch.core.firebase.firestore.domain.model.Constant.USERS
import com.harissabil.fisch.core.firebase.storage.domain.StorageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    @Provides
    @Singleton
    @Named(LOGBOOKS)
    fun provideLogbooksRef() = Firebase.firestore.collection(LOGBOOKS)

    @Provides
    @Singleton
    @Named(MAPS)
    fun provideMapsRef() = Firebase.firestore.collection(MAPS)

    @Provides
    @Singleton
    @Named(USERS)
    fun provideUsersRef() = Firebase.firestore.collection(USERS)

    @Provides
    @Singleton
    fun provideFirestoreRepository(
        @Named(LOGBOOKS) logbooksRef: CollectionReference,
        @Named(MAPS) mapsRef: CollectionReference,
        @Named(USERS) usersRef: CollectionReference,
        storageRepository: StorageRepository,
    ): FirestoreRepository =
        FirestoreRepositoryImpl(
            logbooksRef = logbooksRef,
            mapsRef = mapsRef,
            usersRef = usersRef,
            auth = Firebase.auth,
            storageRepository = storageRepository
        )

    @Provides
    @Singleton
    fun provideUserPlanRepository(
        @Named(USERS) usersRef: CollectionReference,
    ): UserPlanRepository =
        UserPlanRepositoryImpl(
            usersRef = usersRef,
            auth = Firebase.auth,
        )
}