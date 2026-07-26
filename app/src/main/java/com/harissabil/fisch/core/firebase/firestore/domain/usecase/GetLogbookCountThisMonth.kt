package com.harissabil.fisch.core.firebase.firestore.domain.usecase

import com.harissabil.fisch.core.firebase.firestore.domain.UserPlanRepository
import javax.inject.Inject

class GetLogbookCountThisMonth
@Inject
constructor(private val userPlanRepository: UserPlanRepository) {
    operator fun invoke() = userPlanRepository.getLogbookCountThisMonth()
}
