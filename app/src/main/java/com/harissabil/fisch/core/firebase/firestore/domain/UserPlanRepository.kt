package com.harissabil.fisch.core.firebase.firestore.domain

import com.harissabil.fisch.core.common.util.Resource
import com.harissabil.fisch.core.firebase.firestore.domain.model.UserPlan
import kotlinx.coroutines.flow.Flow

interface UserPlanRepository {
    suspend fun getUserPlan(): Resource<UserPlan>

    suspend fun updateUserPlan(userPlan: UserPlan): Resource<Boolean>

    fun getLogbookCountThisMonth(): Flow<Resource<Int>>
}
