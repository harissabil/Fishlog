package com.harissabil.fisch.core.firebase.firestore.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.harissabil.fisch.core.common.util.Resource
import com.harissabil.fisch.core.common.util.currentMonthId
import com.harissabil.fisch.core.firebase.firestore.domain.UserPlanRepository
import com.harissabil.fisch.core.firebase.firestore.domain.model.Constant.COUNTERS
import com.harissabil.fisch.core.firebase.firestore.domain.model.Constant.USERS
import com.harissabil.fisch.core.firebase.firestore.domain.model.LogbookCounter
import com.harissabil.fisch.core.firebase.firestore.domain.model.UserPlan
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserPlanRepositoryImpl @Inject constructor(
    @Named(USERS) private val usersRef: CollectionReference,
    private val auth: FirebaseAuth,
) : UserPlanRepository {

    override suspend fun getUserPlan(): Resource<UserPlan> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Something went wrong!")
        return try {
            val snapshot = usersRef.document(uid).get().await()
            Resource.Success(snapshot.toObject(UserPlan::class.java) ?: UserPlan())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Something went wrong!")
        }
    }

    override suspend fun updateUserPlan(userPlan: UserPlan): Resource<Boolean> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Something went wrong!")
        return try {
            usersRef.document(uid).set(userPlan).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Something went wrong!", false)
        }
    }

    override fun getLogbookCountThisMonth(): Flow<Resource<Int>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("Something went wrong!"))
            awaitClose { }
            return@callbackFlow
        }

        val snapshotListener = usersRef.document(uid).collection(COUNTERS)
            .document(currentMonthId())
            .addSnapshotListener { snapshot, e ->
                val result = if (snapshot != null) {
                    val counter = snapshot.toObject(LogbookCounter::class.java) ?: LogbookCounter()
                    Resource.Success(counter.count)
                } else {
                    Resource.Error(e?.message ?: "Something went wrong!")
                }
                trySend(result)
            }
        awaitClose {
            snapshotListener.remove()
        }
    }
}
