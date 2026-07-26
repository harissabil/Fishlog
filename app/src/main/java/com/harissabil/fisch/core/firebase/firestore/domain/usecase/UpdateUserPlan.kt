package com.harissabil.fisch.core.firebase.firestore.domain.usecase

import com.harissabil.fisch.core.firebase.firestore.domain.UserPlanRepository
import com.harissabil.fisch.core.firebase.firestore.domain.model.UserPlan
import javax.inject.Inject

class UpdateUserPlan @Inject constructor(private val userPlanRepository: UserPlanRepository) {
    suspend operator fun invoke(userPlan: UserPlan) = userPlanRepository.updateUserPlan(userPlan)
}
