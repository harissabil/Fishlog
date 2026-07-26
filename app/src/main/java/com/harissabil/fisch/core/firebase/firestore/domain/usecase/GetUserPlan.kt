package com.harissabil.fisch.core.firebase.firestore.domain.usecase

import com.harissabil.fisch.core.firebase.firestore.domain.UserPlanRepository
import javax.inject.Inject

class GetUserPlan @Inject constructor(private val userPlanRepository: UserPlanRepository) {
    suspend operator fun invoke() = userPlanRepository.getUserPlan()
}
